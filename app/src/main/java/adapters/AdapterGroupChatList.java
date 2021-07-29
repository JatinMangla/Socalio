package adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socalio.GroupChatActivity;
import com.example.socalio.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import models.ModelGroupChatList;


public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList>{

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_grpchats_list,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  AdapterGroupChatList.HolderGroupChatList holder, int position) {
        ModelGroupChatList model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.personnameTv.setText("");
        holder.timeTv.setText("");
        holder.grpmessageTv.setText("");

        loadLastMessage(model, holder);

        holder.grptitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_default_image_white).into(holder.grpIconIv);
        }
        catch (Exception e)
        {
            holder.grpIconIv.setImageResource(R.drawable.ic_default_image_white);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String  dateTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", cal).toString();

                            holder.grpmessageTv.setText(message);
                            holder.timeTv.setText(dateTime);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren())
                                    {
                                        String name = ""+ds.child("name").getValue();
                                        holder.personnameTv.setText(name+": ");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull  DatabaseError error) {

                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{

        private ImageView grpIconIv;
        private TextView grptitleTv,personnameTv,grpmessageTv,timeTv;

        public HolderGroupChatList(@NonNull  View itemView) {
            super(itemView);
            grpIconIv = itemView.findViewById(R.id.grpIconIv);
            grptitleTv = itemView.findViewById(R.id.grptitleTv);
            personnameTv = itemView.findViewById(R.id.personnameTv);
            grpmessageTv = itemView.findViewById(R.id.grpmessageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
