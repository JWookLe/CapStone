package com.example.EmotionSync;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.EmotionSync.model.ChatMessage;
import com.example.EmotionSync.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages = new ArrayList<>();
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;
    private static final Pattern URL_PATTERN = Pattern.compile("emotion-sync://content/([^/]+)/([^/]+)");
    private Context context;
    private RecyclerView recyclerView;

    public ChatAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.isMine() ? VIEW_TYPE_MY_MESSAGE : VIEW_TYPE_OTHER_MESSAGE;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageText.setText(message.getContent());
        holder.messageText.setTextColor(message.isMine() ? Color.WHITE : Color.BLACK);
        holder.messageText.setBackgroundResource(message.isMine() ? R.drawable.bg_message_sent : R.drawable.bg_message_received);
        
        // 메시지 내용에서 URL 찾기
        String content = message.getContent();
        if (content != null) {
            Matcher matcher = URL_PATTERN.matcher(content);
            if (matcher.find()) {
                String url = matcher.group(0);
                SpannableString spannableString = new SpannableString(content);
                
                // URL 부분에 클릭 가능한 스팬 추가
                int startIndex = content.indexOf(url);
                int endIndex = startIndex + url.length();
                
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Log.d("ChatAdapter", "링크 클릭됨: " + url);
                        
                        // URL에서 컨텐츠 타입과 ID 추출
                        String[] pathParts = url.split("/");
                        if (pathParts.length >= 4) {
                            // emotion-sync://content/movie/436969 형식에서
                            // movie가 타입이고 436969가 ID
                            String contentType = pathParts[3];  // movie, music, book
                            String contentId = pathParts[4];    // 실제 ID
                            
                            // video 타입을 youtube로 변환
                            if ("video".equals(contentType)) {
                                contentType = "youtube";
                            }
                            
                            // 메시지에서 공유 정보 추출
                            String messageContent = message.getContent();
                            Log.d("ChatAdapter", "메시지 내용: " + messageContent);
                            
                            String sharedBy = message.getSenderId();
                            String sharedTo = message.getReceiverId();
                            
                            // ContentDetailActivity로 이동
                            Intent intent = new Intent(context, ContentDetailActivity.class);
                            intent.putExtra("contentType", contentType);
                            intent.putExtra("contentId", contentId);
                            
                            if (sharedBy != null && sharedTo != null) {
                                intent.putExtra("sharedBy", sharedBy);
                                intent.putExtra("sharedTo", sharedTo);
                                intent.putExtra("isShared", true);
                            }
                            
                            Log.d("ChatAdapter", "ContentDetailActivity로 이동 - Type: " + contentType + 
                                ", ID: " + contentId + ", SharedBy: " + sharedBy + ", SharedTo: " + sharedTo);
                            
                            context.startActivity(intent);
                        }
                    }
                };
                
                spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                holder.messageText.setText(spannableString);
                holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
        
        // 메시지 레이아웃 정렬
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();
        if (message.isMine()) {
            params.gravity = Gravity.END;
            holder.messageText.setLayoutParams(params);
        } else {
            params.gravity = Gravity.START;
            holder.messageText.setLayoutParams(params);
        }
        
        // 시간 표시
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.timeText.setText(time);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessage> messages) {
        try {
            this.messages = messages;
            notifyDataSetChanged();
            if (recyclerView != null && !messages.isEmpty()) {
                recyclerView.post(() -> {
                    try {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("ChatAdapter", "스크롤 중 오류 발생", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ChatAdapter", "메시지 목록 설정 중 오류 발생", e);
        }
    }

    public void addMessage(ChatMessage message) {
        try {
            Log.d("ChatAdapter", "메시지 추가 시도 - ID: " + message.getMessageId());
            this.messages.add(message);
            notifyItemInserted(messages.size() - 1);
            if (recyclerView != null) {
                recyclerView.post(() -> {
                    try {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("ChatAdapter", "스크롤 중 오류 발생", e);
                    }
                });
            }
            Log.d("ChatAdapter", "메시지 추가 완료 - 현재 메시지 수: " + messages.size());
        } catch (Exception e) {
            Log.e("ChatAdapter", "메시지 추가 중 오류 발생", e);
        }
    }

    public void addMessages(List<ChatMessage> newMessages) {
        try {
            int startPosition = messages.size();
            messages.addAll(newMessages);
            notifyItemRangeInserted(startPosition, newMessages.size());
            if (recyclerView != null) {
                recyclerView.post(() -> {
                    try {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    } catch (Exception e) {
                        Log.e("ChatAdapter", "스크롤 중 오류 발생", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ChatAdapter", "메시지 목록 추가 중 오류 발생", e);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}