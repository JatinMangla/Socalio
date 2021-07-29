package adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socalio.ChatActivity;
import com.example.socalio.R;
import com.example.socalio.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import models.ModelUser;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.Myholder> {

    List<ModelUser> userList;
    Context context;

    public AdapterUsers(List<ModelUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup,false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();

        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image_white).into(holder.mAvatarIv);
        }
        catch (Exception e)
        {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, ""+userEmail, Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which==0)
                        {
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);
                        }
                        if (which==1)
                        {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid",hisUID);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();

            }

        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class Myholder extends RecyclerView.ViewHolder {

        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;
        public Myholder(@NonNull View itemView) {
            super(itemView);

            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
