from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import json
import logging
from recommend_service import recommend_by_emotion_code
from datetime import datetime, timedelta
import random
import os
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

BASE_DIR = Path(__file__).resolve().parent

if 'load_dotenv' in globals() and load_dotenv:
    load_dotenv(BASE_DIR / '.env')

TMDB_API_KEY = os.getenv('TMDB_API_KEY', '')
YOUTUBE_API_KEYS = [key.strip() for key in os.getenv('YOUTUBE_API_KEYS', '').split(',') if key.strip()]
single_youtube_key = os.getenv('YOUTUBE_API_KEY', '').strip()
if single_youtube_key and single_youtube_key not in YOUTUBE_API_KEYS:
    YOUTUBE_API_KEYS.append(single_youtube_key)

if not TMDB_API_KEY:
    logger.warning('TMDB_API_KEY is not set. TMDB requests will fail.')
if not YOUTUBE_API_KEYS:
    logger.warning('YOUTUBE_API_KEYS is not set. YouTube requests will fail.')

# 스프링# 스프링 부트 서버 URL
SPRING_SERVER_URL = os.getenv('SPRING_SERVER_URL', 'http://localhost:8080')

def get_youtube_api_key():
    """Return a configured YouTube API key or None if unavailable."""
    if not YOUTUBE_API_KEYS:
        logger.error('No YouTube API keys configured. Skipping YouTube request.')
        return None
    return random.choice(YOUTUBE_API_KEYS)


# 감정 코드와 키워드 매핑
mood_keywords = {
    "happy": ["happy comedy feel good", "uplifting comedy", "feel good movie", "happy music", "cheerful songs"],
    "sad": ["sad drama emotional", "melodrama", "emotional story", "sad music", "emotional songs"],
    "angry": ["action revenge thriller", "action drama", "revenge story", "angry music", "intense songs"],
    "anxious": ["calm peaceful relaxing", "healing movie", "peaceful story", "calm music", "relaxing songs"]
}

# 감정별 장르 ID 매핑
emotion_genres = {
    "happy": ["35", "10751"],  # Comedy + Family
    "sad": ["18", "10749"],    # Drama + Romance
    "angry": ["28,53", "80,53"],  # Action+Thriller or Crime+Thriller
    "anxious": ["18,9648", "18,14"] # Drama+Mystery or Drama+Fantasy
}

def map_emotion_type_to_keyword(emotion_type):
    emotion_map = {
        "기쁨": "happy",
        "슬픔": "sad",
        "화남": "angry",
        "불안": "anxious"
    }
    mapped_keyword = emotion_map.get(emotion_type, "healing")
    logger.info(f"감정 코드 매핑: {emotion_type} -> {mapped_keyword}")
    return mapped_keyword

def get_cached_movies(keyword, region):
    """캐시에서 영화 정보를 가져옵니다."""
    cache_key = f"{keyword}_{region}"
    if cache_key in movie_cache:
        cache_data = movie_cache[cache_key]
        if datetime.now() - cache_data['timestamp'] < CACHE_DURATION:
            logger.info(f"캐시에서 영화 정보 로드: {cache_key}")
            return cache_data['movies']
    return None

def cache_movies(keyword, region, movies):
    """영화 정보를 캐시에 저장합니다."""
    cache_key = f"{keyword}_{region}"
    movie_cache[cache_key] = {
        'movies': movies,
        'timestamp': datetime.now()
    }
    logger.info(f"영화 정보 캐시 저장: {cache_key}")

def search_movies(keyword, region):
    """TMDB API
    if not TMDB_API_KEY:
        logger.error('TMDB_API_KEY is not configured.')
        return []

를 사용하여 영화를 검색합니다."""
    # API 요청
    language = "ko-KR" if region == "KR" else "en-US"
    
    # 랜덤하게 키워드와 장르 선택
    search_query = random.choice(mood_keywords[keyword])
    genre_id = random.choice(emotion_genres[keyword])
    
    # 검색 쿼리 로깅
    logger.info(f"검색 쿼리 ({region}): {search_query}")
    logger.info(f"장르 ID ({region}): {genre_id}")
    
    # 랜덤 페이지 번호 (1-5)
    page = random.randint(1, 5)
    
    # Discover API 사용
    url = f"https://api.themoviedb.org/3/discover/movie?api_key={TMDB_API_KEY}&language={language}&region={region}&with_genres={genre_id}&sort_by=popularity.desc&include_adult=false&page={page}"
    
    logger.info(f"TMDB API 요청 URL ({region}): {url}")
    
    try:
        response = requests.get(url)
        logger.info(f"TMDB API 응답 상태 코드 ({region}): {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            total_results = data.get("total_results", 0)
            logger.info(f"검색 결과 수 ({region}): {total_results}")
            
            movies = data.get("results", [])
            if movies:
                # 랜덤하게 2개 영화 선택
                selected_movies = random.sample(movies, min(2, len(movies)))
                for movie in selected_movies:
                    logger.info(f"선택된 영화 ({region}): {movie.get('title')}")
                return selected_movies
            else:
                logger.warning(f"검색 결과 없음 ({region})")
                return []
        else:
            logger.error(f"TMDB API 오류 ({region}): {response.text}")
            return []
    except Exception as e:
        logger.error(f"TMDB API 요청 중 오류 발생 ({region}): {e}")
        return []

def get_user_emotion_data(user_id):
    """스프링 부트 서버에서 사용자의 최근 감정 데이터를 가져옵니다."""
    try:
        emotion_response = requests.get(f"{SPRING_SERVER_URL}/api/emotions/latest/{user_id}")
        if emotion_response.status_code == 200:
            emotion_data = emotion_response.json()
            logger.info(f"사용자 감정 데이터 조회 성공: {user_id}")
            return emotion_data
        logger.warning(f"사용자 감정 데이터 조회 실패: {user_id}")
        return None
    except Exception as e:
        logger.error(f"사용자 감정 데이터 조회 중 오류 발생: {e}")
        return None

def get_survey_data(user_id):
    """스프링 부트 서버에서 사용자의 최근 설문 데이터를 가져옵니다."""
    try:
        survey_response = requests.get(f"{SPRING_SERVER_URL}/api/surveys/latest/{user_id}")
        if survey_response.status_code == 200:
            survey_data = survey_response.json()
            logger.info(f"사용자 설문 데이터 조회 성공: {user_id}")
            return survey_data
        logger.warning(f"사용자 설문 데이터 조회 실패: {user_id}")
        return None
    except Exception as e:
        logger.error(f"사용자 설문 데이터 조회 중 오류 발생: {e}")
        return None

def search_youtube_content(query, region, content_type):
    """YouTube API를 사용하여 콘텐츠를 검색합니다."""
    try:
        # 검색 쿼리 로깅
        logger.info(f"YouTube 검색 쿼리 ({region}, {content_type}): {query}")
        
        # 검색 파라미터 설정
        api_key = get_youtube_api_key()
        if not api_key:
            return []
        params = {
            'part': 'snippet',
            'q': query,
            'type': 'video',
            'key': api_key,
            'maxResults': 2,
            'relevanceLanguage': 'ko' if region == 'KR' else 'en',
            'videoCategoryId': '10' if content_type == 'music' else '28'  # 10: 음악, 28: 과학/기술
        }
        
        # API 요청
        response = requests.get('https://www.googleapis.com/youtube/v3/search', params=params)
        logger.info(f"YouTube API 응답 상태 코드 ({region}, {content_type}): {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            items = data.get('items', [])
            logger.info(f"YouTube 검색 결과 수 ({region}, {content_type}): {len(items)}")
            
            results = []
            for item in items:
                video_id = item['id']['videoId']
                results.append({
                    'type': content_type,
                    'title': item['snippet']['title'],
                    'source': 'YouTube',
                    'image_url': f"https://img.youtube.com/vi/{video_id}/hqdefault.jpg",
                    'link_url': f"https://www.youtube.com/watch?v={video_id}",
                    'region': region
                })
            return results
        else:
            logger.error(f"YouTube API 오류 ({region}, {content_type}): {response.text}")
            return []
    except Exception as e:
        logger.error(f"YouTube API 요청 중 오류 발생 ({region}, {content_type}): {e}")
        return []

def recommend_by_emotion_code(emotion_code, user_id=None, q1=None, q2=None, q3=None, q4=None, q5=None):
    """감정 코드와 사용자 ID를 기반으로 컨텐츠를 추천합니다."""
    logger.info(f"추천 요청: emotion={emotion_code}, user_id={user_id}")
    
    # recommend_service의 recommend_by_emotion_code 함수 호출
    from recommend_service import recommend_by_emotion_code as service_recommend
    return service_recommend(emotion_code, user_id, q1, q2, q3, q4, q5)

# 공통 포맷팅 함수
def format_recommendations(recommendations):
    formatted = []
    for item in recommendations:
        formatted.append({
            "type": item.get("type", "music"),
            "title": item.get("title", "Unknown Title"),
            "source": item.get("source", "YouTube"),
            "image_url": item.get("image_url", ""),
            "link_url": item.get("link_url", ""),
            "region": item.get("region", "")
        })
    return formatted

# GET 방식 추천 API
@app.route("/recommend", methods=["GET"])
def recommend():
    """감정 기반 추천 API"""
    try:
        # 요청 파라미터 가져오기
        emotion = request.args.get('emotion')
        user_id = request.args.get('user_id')
        q1 = request.args.get('q1')
        q2 = request.args.get('q2')
        q3 = request.args.get('q3')
        q4 = request.args.get('q4')
        q5 = request.args.get('q5')
        
        # recommend_service의 recommend_by_emotion_code 함수 호출
        recommendations = recommend_by_emotion_code(emotion, user_id, q1, q2, q3, q4, q5)
        
        return jsonify(recommendations)
    except Exception as e:
        logger.error(f"추천 API 오류: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 특정 타입의 콘텐츠만 추천하는 API
@app.route("/recommend/<content_type>", methods=["GET"])
def recommend_by_type(content_type):
    emotion = request.args.get("emotion")
    user_id = request.args.get("user_id")
    
    if not emotion:
        return jsonify({"error": "emotion query parameter is required"}), 400

    recommendations = recommend_by_emotion_code(emotion, user_id)
    
    # content_type (music, movie, video) 필터링
    filtered = [item for item in recommendations if item.get("type") == content_type]
    logger.info(f"필터링된 {content_type} 추천 결과: {len(filtered)}개")

    if not filtered:
        return jsonify({"error": f"No {content_type} recommendations found for emotion {emotion}"}), 404

    formatted = format_recommendations(filtered)
    return jsonify(formatted)

@app.route("/content/details", methods=["GET"])
def get_content_details():
    """컨텐츠 상세 정보를 반환하는 API"""
    try:
        content_type = request.args.get('type')
        content_id = request.args.get('id')
        region = request.args.get('region', 'KR')
        
        if not content_type or not content_id:
            return jsonify({"error": "type and id parameters are required"}), 400
            
        if content_type in ['music', 'video']:  # music과 video 모두 YouTube API 사용
            # YouTube 음악/영상 상세 정보
            youtube = get_youtube_client()
            if not youtube:
                return jsonify({"error": "YouTube API is not available"}), 500
                
            video_response = youtube.videos().list(
                part="snippet,contentDetails,statistics",
                id=content_id
            ).execute()
            
            if not video_response.get('items'):
                return jsonify({"error": "Content not found"}), 404
                
            video = video_response['items'][0]
            details = {
                'title': video['snippet']['title'],
                'description': video['snippet']['description'],
                'thumbnail': video['snippet']['thumbnails']['high']['url'],
                'channelTitle': video['snippet']['channelTitle'],
                'publishedAt': video['snippet']['publishedAt'],
                'viewCount': video['statistics'].get('viewCount', '0'),
                'likeCount': video['statistics'].get('likeCount', '0'),
                'commentCount': video['statistics'].get('commentCount', '0'),
                'duration': video['contentDetails']['duration'],
                'url': f"https://www.youtube.com/watch?v={content_id}",
                'tags': video['snippet'].get('tags', []),  # 태그 정보 추가
                'categoryId': video['snippet'].get('categoryId', ''),  # 카테고리 ID 추가
                'liveBroadcastContent': video['snippet'].get('liveBroadcastContent', 'none')  # 라이브 방송 여부
            }
            
        elif content_type == 'movie':
            if not TMDB_API_KEY:
                logger.error('TMDB API key is not configured; cannot fetch movie details.')
                return jsonify({'error': 'TMDB API key is not configured'}), 500
            # TMDB 영화 상세 정보
            details_url = f"https://api.themoviedb.org/3/movie/{content_id}"
            details_params = {
                'api_key': TMDB_API_KEY,
                'language': 'ko-KR' if region == 'KR' else 'en-US',
                'append_to_response': 'credits,videos'
            }
            
            details_response = requests.get(details_url, params=details_params)
            if details_response.status_code != 200:
                return jsonify({"error": "Failed to fetch movie details"}), 500
                
            movie = details_response.json()
            details = {
                'title': movie['title'],
                'overview': movie['overview'],
                'poster_path': f"https://image.tmdb.org/t/p/original{movie['poster_path']}",
                'backdrop_path': f"https://image.tmdb.org/t/p/original{movie['backdrop_path']}",
                'release_date': movie['release_date'],
                'runtime': movie['runtime'],
                'vote_average': movie['vote_average'],
                'genres': [genre['name'] for genre in movie['genres']],
                'director': next((crew['name'] for crew in movie['credits']['crew'] if crew['job'] == 'Director'), ''),
                'cast': [cast['name'] for cast in movie['credits']['cast'][:5]],
                'url': f"https://www.themoviedb.org/movie/{content_id}"
            }
            
        else:
            return jsonify({"error": "Unsupported content type"}), 400
            
        return jsonify(details)
        
    except Exception as e:
        logger.error(f"컨텐츠 상세 정보 조회 중 오류 발생: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 주피터 노트북에서 실행하기 위한 함수
def run_flask_app():
    """Flask 앱 실행"""
    app.run(host='0.0.0.0', port=5000, debug=True)

if __name__ == "__main__":
    run_flask_app() 