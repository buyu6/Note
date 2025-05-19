package com.example.newnote;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<Note> mNoteList;
    private ItemClickListener listener,s;
    public NoteAdapter(List<Note> NoteList) {
       mNoteList = NoteList;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }
public void setItemLongClickListener( ItemClickListener s){
        this.s=s;
}
    static class ViewHolder extends RecyclerView.ViewHolder{
        View noteView;
       TextView title;
       TextView content;
        public ViewHolder(View view){
            super(view);
            noteView=view;
            title=(TextView) view.findViewById(R.id.text_view);
            content=(TextView) view.findViewById(R.id.ctext_view);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,parent,false );
        final ViewHolder holder=new ViewHolder(view);
        holder.noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position= holder.getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION) {//防止item被移除后的无效位置
                    //通过接口回调点击事件
                    listener.onItemClick(position);
                }
            }
        });
        holder.noteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position= holder.getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION) {//防止item被移除后的无效位置
                    //通过接口回调点击事件
                    s.onItemLongClick(position);
                }
                return false;
            }
        });
        return holder;
    }
    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
Note note=mNoteList.get(position);
holder.title.setText(note.getTitle());
holder.content.setText(note.getContent());
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }
    public void refreshData(List<Note> notes){
        this.mNoteList=notes;
        notifyDataSetChanged();
    }

}
