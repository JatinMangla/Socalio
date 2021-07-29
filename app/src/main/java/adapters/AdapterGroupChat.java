package adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socalio.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import models.ModelGroupChat;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChats;

    private  FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChats) {
        this.context = context;
        this.modelGroupChats = modelGroupChats;

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_grpchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_grpchat_left,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull  AdapterGroupChat.HolderGroupChat holder, int position) {
        ModelGroupChat model = modelGroupChats.get(position);

        String message = model.getMessage();
        String timestamp = model.getTimestamp();
        String senderUid  = model.getSender();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String  dateTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", cal).toString();


        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        setUserName(model,holder);
    }

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String name = ""+ds.child("name").getValue();

                    holder.nameTv.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChats.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
                return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return modelGroupChats.size();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv;
        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
