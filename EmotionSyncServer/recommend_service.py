import requests
import logging
import tensorflow as tf
import numpy as np
import pickle
from tensorflow.keras.preprocessing.sequence import pad_sequences
import random
import os
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    load_dotenv = None

from googleapiclient.discovery import build

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# TMDB와 YouTube API 키는 환경 변수에서 로드
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
    logger.warning('YOUTUBE_API_KEYS is not set. YouTube features will be disabled.')

SPRING_SERVER_URL = os.getenv('SPRING_SERVER_URL', 'http://localhost:8080')

# 케라스 모델 로드
try:
    # TensorFlow 호환성 설정
    tf.keras.backend.clear_session()
    tf.config.run_functions_eagerly(True)
    
    # 모델 로드
    emotion_model = tf.keras.models.load_model('final_emotion_model.h5', 
                                             compile=False)
    emotion_model.compile(loss='categorical_crossentropy', 
                        optimizer='adam', 
                        metrics=['accuracy'])
    
    logger.info("케라스 모델 로드 성공")
except Exception as e:
    logger.error(f"케라스 모델 로드 실패: {e}")
    emotion_model = None

# 감정 코드와 키워드 매핑
EMOTION_MAPPING = {
    'E37': {  # 불안/긴장
        'keywords': {
            'ko': ['불안', '긴장', '걱정', '초조', '두려움'],
            'en': ['anxious', 'nervous', 'worry', 'stress', 'fear']
        }
    },
    'E66': {  # 기쁨
        'keywords': {
            'ko': ['행복', '기쁨', '즐거움', '웃음', '신나'],
            'en': ['happy', 'joy', 'cheerful', 'delight', 'excited']
        }
    },
    'E18': {  # 화남
        'keywords': {
            'ko': ['화남', '분노', '짜증', '불만', '격분'],
            'en': ['angry', 'rage', 'fury', 'irritated', 'frustrated']
        }
    },
    'E40': {  # 슬픔
        'keywords': {
            'ko': ['슬픔', '우울', '눈물', '그리움', '아픔'],
            'en': ['sad', 'depressed', 'melancholy', 'grief', 'heartbreak']
        }
    }
}

# 감정별 검색 키워드
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

def get_search_keywords(emotion_code):
    """감정 코드에 대한 검색 키워드를 반환"""
    # 감정 코드를 키워드로 매핑
    emotion_map = {
        'E37': 'anxious',  # 불안/긴장
        'E66': 'happy',    # 기쁨
        'E18': 'angry',    # 화남
        'E40': 'sad'       # 슬픔
    }
    
    # 감정 코드를 키워드로 변환
    keyword = emotion_map.get(emotion_code, 'happy')
    
    # 해당 키워드에 대한 검색어 목록에서 랜덤 선택
    search_keywords = random.choice(mood_keywords.get(keyword, mood_keywords['happy']))
    
    logger.info(f"생성된 검색 키워드: {search_keywords}")
    return search_keywords

def search_tmdb_movies(keyword, language='ko-KR'):
    """TMDB API를 사용하여 영화 검색"""
    if not TMDB_API_KEY:
        logger.error('TMDB_API_KEY is not configured.')
        return {'results': []}

    try:
        # 검색 API
        search_url = f"https://api.themoviedb.org/3/search/movie"
        search_params = {
            'api_key': TMDB_API_KEY,
            'query': keyword,
            'language': language,
            'page': 1,
            'include_adult': False,
            'region': 'KR' if language == 'ko-KR' else 'US',
            'sort_by': 'popularity.desc',
            'vote_count.gte': 1000,
            'vote_average.gte': 7.0
        }
        
        search_response = requests.get(search_url, params=search_params)
        search_data = search_response.json()
        
        if search_response.status_code != 200:
            logger.error(f"TMDB API 오류: {search_response.status_code} - {search_data}")
            return {'results': []}
        
        # 검색 결과가 없으면 장르 기반 검색
        if not search_data.get('results'):
            logger.info("검색 결과 없음, 장르 기반 검색 시도")
            genre_map = {
                'E37': '53',  # 불안/긴장 -> 스릴러
                'E66': '35',  # 기쁨 -> 코미디
                'E18': '28',  # 화남 -> 액션
                'E40': '18'   # 슬픔 -> 드라마
            }
            
            genre_id = None
            for emotion_code, gid in genre_map.items():
                if emotion_code in keyword:
                    genre_id = gid
                    break
            
            if not genre_id:
                genre_id = '35'
            
            discover_url = f"https://api.themoviedb.org/3/discover/movie"
            discover_params = {
                'api_key': TMDB_API_KEY,
                'language': language,
                'with_genres': genre_id,
                'sort_by': 'popularity.desc',
                'include_adult': False,
                'page': 1,
                'region': 'KR' if language == 'ko-KR' else 'US',
                'vote_count.gte': 1000,
                'vote_average.gte': 7.0
            }
            
            discover_response = requests.get(discover_url, params=discover_params)
            discover_data = discover_response.json()
            
            if discover_response.status_code != 200:
                logger.error(f"TMDB 장르 검색 API 오류: {discover_response.status_code} - {discover_data}")
                return {'results': []}
            
            search_data = discover_data
        
        # 각 영화의 상세 정보 가져오기
        for movie in search_data.get('results', []):
            movie_id = movie['id']
            details_url = f"https://api.themoviedb.org/3/movie/{movie_id}"
            details_params = {
                'api_key': TMDB_API_KEY,
                'language': language,
                'append_to_response': 'credits,videos'
            }
            
            details_response = requests.get(details_url, params=details_params)
            if details_response.status_code == 200:
                details = details_response.json()
                movie['details'] = {
                    'title': details['title'],
                    'overview': details['overview'],
                    'poster_path': f"https://image.tmdb.org/t/p/original{details['poster_path']}",
                    'backdrop_path': f"https://image.tmdb.org/t/p/original{details['backdrop_path']}",
                    'release_date': details['release_date'],
                    'runtime': details['runtime'],
                    'vote_average': details['vote_average'],
                    'genres': [genre['name'] for genre in details['genres']],
                    'director': next((crew['name'] for crew in details['credits']['crew'] if crew['job'] == 'Director'), ''),
                    'cast': [cast['name'] for cast in details['credits']['cast'][:5]],
                    'url': f"https://www.themoviedb.org/movie/{movie_id}"
                }
        
        return search_data
    except Exception as e:
        logger.error(f"TMDB API 오류: {str(e)}")
        return {'results': []}

def get_youtube_client():
    """사용 가능한 YouTube API 키로 클라이언트를 생성"""
    if not YOUTUBE_API_KEYS:
        logger.error('No YouTube API keys configured. Skipping YouTube client creation.')
        return None

    for api_key in YOUTUBE_API_KEYS:
        try:
            youtube = build('youtube', 'v3', developerKey=api_key)
            # 간단한 테스트 요청으로 API 키 유효성 확인
            youtube.search().list(part='snippet', q='test', maxResults=1).execute()
            return youtube
        except Exception as e:
            logger.error(f"YouTube API 키 오류: {str(e)}")
            continue
    return None

def search_youtube_videos(keyword, language='ko', content_type='video'):
    """YouTube API를 사용하여 영상 검색"""
    try:
        youtube = get_youtube_client()
        if not youtube:
            logger.error("사용 가능한 YouTube API 키가 없습니다.")
            return {'items': []}
        
        # 검색 요청
        search_response = youtube.search().list(
            q=keyword,
            part='snippet',
            maxResults=5,
            type=content_type,
            relevanceLanguage=language,
            regionCode='KR' if language == 'ko' else 'US'
        ).execute()
        
        # 검색 결과가 없으면 영어로 재시도
        if not search_response.get('items') and language == 'ko':
            logger.info("한국어 검색 결과 없음, 영어로 재시도")
            return search_youtube_videos(keyword, language='en', content_type=content_type)
        
        # 각 영상의 상세 정보 가져오기
        video_ids = [item['id']['videoId'] for item in search_response.get('items', [])]
        if video_ids:
            video_response = youtube.videos().list(
                part="snippet,contentDetails,statistics",
                id=','.join(video_ids)
            ).execute()
            
            # 검색 결과에 상세 정보 추가
            for item in search_response.get('items', []):
                video_id = item['id']['videoId']
                video_details = next((v for v in video_response.get('items', []) if v['id'] == video_id), None)
                if video_details:
                    item['details'] = {
                        'title': video_details['snippet']['title'],
                        'description': video_details['snippet']['description'],
                        'thumbnail': video_details['snippet']['thumbnails']['high']['url'],
                        'channelTitle': video_details['snippet']['channelTitle'],
                        'publishedAt': video_details['snippet']['publishedAt'],
                        'viewCount': video_details['statistics'].get('viewCount', '0'),
                        'likeCount': video_details['statistics'].get('likeCount', '0'),
                        'commentCount': video_details['statistics'].get('commentCount', '0'),
                        'duration': video_details['contentDetails']['duration'],
                        'url': f"https://www.youtube.com/watch?v={video_id}"
                    }
        
        return search_response
    except Exception as e:
        logger.error(f"YouTube API 오류: {str(e)}")
        return {'items': []}

def predict_emotion(survey_data):
    """설문 데이터를 기반으로 감정을 예측합니다."""
    if emotion_model is None:
        return None
    
    try:
        # 설문 데이터의 텍스트 응답을 하나의 문자열로 결합
        text_input = f"{survey_data.get('question5', '')} {survey_data.get('question6', '')}"
        
        # 토크나이저와 레이블 인코더 로드
        with open('tokenizer.pickle', 'rb') as handle:
            tokenizer = pickle.load(handle)
        with open('label_encoder.pickle', 'rb') as handle:
            label_encoder = pickle.load(handle)
        
        # 텍스트를 시퀀스로 변환
        sequence = tokenizer.texts_to_sequences([text_input])
        padded = pad_sequences(sequence, maxlen=50)
        
        # 감정 예측
        prediction = emotion_model.predict(padded)
        emotion_idx = np.argmax(prediction[0])
        
        # 레이블 인코더를 사용하여 감정 매핑
        predicted_emotion = label_encoder.inverse_transform([emotion_idx])[0]
        logger.info(f"감정 예측 결과: {predicted_emotion}")
        return predicted_emotion
    except Exception as e:
        logger.error(f"감정 예측 중 오류 발생: {e}")
        return None

def get_user_emotion_data(user_id):
    """스프링 부트 서버에서 사용자의 최근 감정 데이터를 가져옵니다."""
    try:
        # 최근 감정 기록 조회
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
        # 최근 설문 기록 조회
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

def get_tmdb_keyword_id(keyword):
    """감정 키워드에 맞는 TMDB 키워드 ID를 반환합니다."""
    keyword_map = {
        "angry": "1803",  # 분노/화남
        "happy": "1747",  # 기쁨/행복
        "sad": "1750",    # 슬픔/우울
        "anxious": "1749" # 불안/긴장
    }
    return keyword_map.get(keyword, "1747")  # 기본값은 기쁨/행복

def recommend_by_emotion_code(emotion, user_id=None, q1=None, q2=None, q3=None, q4=None, q5=None):
    """설문 결과를 기반으로 감정을 분석하고 컨텐츠를 추천합니다."""
    logger.info(f"추천 요청: emotion={emotion}, user_id={user_id}")
    
    # 입력된 감정이 있으면 그것을 우선적으로 사용
    if emotion:
        logger.info(f"입력된 감정 사용: {emotion}")
    # 그렇지 않은 경우에만 모델 예측 사용
    elif all(x is not None for x in [q1, q2, q3, q4, q5]):
        survey_data = {
            'question1': q1,
            'question2': q2,
            'question3': q3,
            'question4': q4,
            'question5': q5
        }
        if emotion_model:
            predicted_emotion = predict_emotion(survey_data)
            if predicted_emotion:
                emotion = predicted_emotion
                logger.info(f"예측된 감정 데이터 사용: {emotion}")
    elif user_id:
        emotion_data = get_user_emotion_data(user_id)
        survey_data = get_survey_data(user_id)
        
        if survey_data and emotion_model:
            predicted_emotion = predict_emotion(survey_data)
            if predicted_emotion:
                emotion = predicted_emotion
                logger.info(f"예측된 감정 데이터 사용: {emotion}")
        elif emotion_data and emotion_data.get('emotion_type'):
            emotion = emotion_data['emotion_type']
            logger.info(f"DB에서 가져온 감정 데이터 사용: {emotion}")

    # 감정 코드를 키워드로 변환
    search_keywords = get_search_keywords(emotion)
    logger.info(f"검색 키워드: {search_keywords}")
    
    results = []
    seen_video_ids = set()  # 중복 제거를 위한 세트
    seen_movie_ids = set()  # 영화 중복 제거를 위한 세트
    seen_movie_titles = set()  # 영화 제목 중복 제거를 위한 세트

    # 🎬 TMDB 영화 추천 (한국과 미국 영화)
    # 한국 영화 검색
    ko_movies = search_tmdb_movies(search_keywords, 'ko-KR')
    if ko_movies.get('results'):
        for movie in ko_movies['results']:
            movie_id = movie.get('id')
            movie_title = movie.get('title', '').lower()  # 제목을 소문자로 변환
            
            # 중복 체크 (ID와 제목 모두 확인)
            if movie_id not in seen_movie_ids and movie_title not in seen_movie_titles:
                seen_movie_ids.add(movie_id)
                seen_movie_titles.add(movie_title)
                poster_path = movie.get('poster_path')
                results.append({
                    "type": "movie",
                    "id": str(movie_id),  # ID 필드 추가
                    "title": movie.get("title"),
                    "source": "TMDB",
                    "image_url": f"https://image.tmdb.org/t/p/w500{poster_path}" if poster_path else "",
                    "link_url": f"https://www.themoviedb.org/movie/{movie_id}",
                    "region": "KR"
                })
                if len([r for r in results if r['type'] == 'movie' and r['region'] == 'KR']) >= 3:
                    break
        logger.info(f"한국 영화 추천 완료: {len([r for r in results if r['type'] == 'movie' and r['region'] == 'KR'])}개")

    # 미국 영화 검색
    en_movies = search_tmdb_movies(search_keywords, 'en-US')
    if en_movies.get('results'):
        for movie in en_movies['results']:
            movie_id = movie.get('id')
            movie_title = movie.get('title', '').lower()  # 제목을 소문자로 변환
            
            # 중복 체크 (ID와 제목 모두 확인)
            if movie_id not in seen_movie_ids and movie_title not in seen_movie_titles:
                seen_movie_ids.add(movie_id)
                seen_movie_titles.add(movie_title)
                poster_path = movie.get('poster_path')
                results.append({
                    "type": "movie",
                    "id": str(movie_id),  # ID 필드 추가
                    "title": movie.get("title"),
                    "source": "TMDB",
                    "image_url": f"https://image.tmdb.org/t/p/w500{poster_path}" if poster_path else "",
                    "link_url": f"https://www.themoviedb.org/movie/{movie_id}",
                    "region": "US"
                })
                if len([r for r in results if r['type'] == 'movie' and r['region'] == 'US']) >= 3:
                    break
        logger.info(f"미국 영화 추천 완료: {len([r for r in results if r['type'] == 'movie' and r['region'] == 'US'])}개")

    # 🎵 YouTube 음악 추천 (한국과 미국 음악)
    # 한국 음악 검색
    ko_music = search_youtube_videos(search_keywords, 'ko', 'music')
    if ko_music.get('items'):
        for video in ko_music['items']:
            video_id = video["id"]["videoId"]
            if video_id not in seen_video_ids:  # 중복 체크
                seen_video_ids.add(video_id)
                results.append({
                    "type": "music",
                    "id": video_id,  # ID 필드 추가
                    "title": video["snippet"]["title"],
                    "source": "YouTube",
                    "image_url": f"https://img.youtube.com/vi/{video_id}/hqdefault.jpg",
                    "link_url": f"https://www.youtube.com/watch?v={video_id}",
                    "region": "KR"
                })
                if len([r for r in results if r['type'] == 'music' and r['region'] == 'KR']) >= 3:
                    break
        logger.info(f"한국 음악 추천 완료: {len([r for r in results if r['type'] == 'music' and r['region'] == 'KR'])}개")

    # 미국 음악 검색
    en_music = search_youtube_videos(search_keywords, 'en', 'music')
    if en_music.get('items'):
        for video in en_music['items']:
            video_id = video["id"]["videoId"]
            if video_id not in seen_video_ids:  # 중복 체크
                seen_video_ids.add(video_id)
                results.append({
                    "type": "music",
                    "id": video_id,  # ID 필드 추가
                    "title": video["snippet"]["title"],
                    "source": "YouTube",
                    "image_url": f"https://img.youtube.com/vi/{video_id}/hqdefault.jpg",
                    "link_url": f"https://www.youtube.com/watch?v={video_id}",
                    "region": "US"
                })
                if len([r for r in results if r['type'] == 'music' and r['region'] == 'US']) >= 3:
                    break
        logger.info(f"미국 음악 추천 완료: {len([r for r in results if r['type'] == 'music' and r['region'] == 'US'])}개")

    # 📺 YouTube 일반 영상 추천 (한국과 미국 영상)
    # 한국 영상 검색
    ko_videos = search_youtube_videos(search_keywords, 'ko', 'video')
    if ko_videos.get('items'):
        for video in ko_videos['items']:
            video_id = video["id"]["videoId"]
            if video_id not in seen_video_ids:  # 중복 체크
                seen_video_ids.add(video_id)
                results.append({
                    "type": "video",
                    "id": video_id,  # ID 필드 추가
                    "title": video["snippet"]["title"],
                    "source": "YouTube",
                    "image_url": f"https://img.youtube.com/vi/{video_id}/hqdefault.jpg",
                    "link_url": f"https://www.youtube.com/watch?v={video_id}",
                    "region": "KR"
                })
                if len([r for r in results if r['type'] == 'video' and r['region'] == 'KR']) >= 3:
                    break
        logger.info(f"한국 영상 추천 완료: {len([r for r in results if r['type'] == 'video' and r['region'] == 'KR'])}개")

    # 미국 영상 검색
    en_videos = search_youtube_videos(search_keywords, 'en', 'video')
    if en_videos.get('items'):
        for video in en_videos['items']:
            video_id = video["id"]["videoId"]
            if video_id not in seen_video_ids:  # 중복 체크
                seen_video_ids.add(video_id)
                results.append({
                    "type": "video",
                    "id": video_id,  # ID 필드 추가
                    "title": video["snippet"]["title"],
                    "source": "YouTube",
                    "image_url": f"https://img.youtube.com/vi/{video_id}/hqdefault.jpg",
                    "link_url": f"https://www.youtube.com/watch?v={video_id}",
                    "region": "US"
                })
                if len([r for r in results if r['type'] == 'video' and r['region'] == 'US']) >= 3:
                    break
        logger.info(f"미국 영상 추천 완료: {len([r for r in results if r['type'] == 'video' and r['region'] == 'US'])}개")

    logger.info(f"전체 추천 결과: {len(results)}개")
    return results