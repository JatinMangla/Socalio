package adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socalio.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import fragments.UsersFragment;
import models.ModelUser;

public class AdapterParticipantsAdd extends RecyclerView.Adapter<AdapterParticipantsAdd.HolderParticipantsAdd> {

    private Context context;
    private ArrayList<ModelUser> userList;
    private  String groupId, myGroupRole;

    public AdapterParticipantsAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participants_add,parent,false);


        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  AdapterParticipantsAdd.HolderParticipantsAdd holder, int position) {
        ModelUser modelUser = userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        String uid = modelUser.getUid();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_image_white).into(holder.avatarIv);
        }
        catch (Exception e)
        {
            holder.avatarIv.setImageResource(R.drawable.ic_default_image_white);
        }

        checkIfAlreadyExists(modelUser, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(   new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    String hispreviousRole = ""+snapshot.child("role").getValue();

                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("choose option");
                                    if (myGroupRole.equals("creator"))
                                    {
                                        if (hispreviousRole.equals("admin"))
                                        {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which ==0)
                                                    {
                                                        removeAdmin(modelUser);
                                                    }
                                                    else
                                                    {
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hispreviousRole.equals("participants"))
                                        {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which ==0)
                                                    {
                                                        makeAdmin(modelUser);
                                                    }
                                                    else
                                                    {
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin"))
                                    {
                                        if (hispreviousRole.equals("creator"))
                                        {
                                            Toast.makeText(context, "Creator of the Grp", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hispreviousRole.equals("admin"))
                                        {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which ==0)
                                                    {
                                                        removeAdmin(modelUser);
                                                    }
                                                    else
                                                    {
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hispreviousRole.equals("participants"))
                                        {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which ==0)
                                                    {
                                                        makeAdmin(modelUser);
                                                    }
                                                    else
                                                    {
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }

                                }
                                else
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant").setMessage("Add this user in this group ?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addParticipant(modelUser);
                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });
            }
        });
    }

    private void addParticipant(ModelUser modelUser) {

        String timestamp  = ""+System.currentTimeMillis();
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("uid",modelUser.getUid());
        hashMap.put("role", "participants");
        hashMap.put("timestamp" , ""+timestamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Added succesfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ModelUser modelUser) {

        String timestamp  = ""+System.currentTimeMillis();
        HashMap<String , Object > hashMap = new HashMap<>();
        hashMap.put("role","admin");
        hashMap.put("uid",modelUser.getUid());
        hashMap.put("timestamp" , ""+timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "the user is now admin....", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeParticipant(ModelUser modelUser) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void removeAdmin(ModelUser modelUser) {

        String timestamp  = ""+System.currentTimeMillis();
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        hashMap.put("uid",modelUser.getUid());
        hashMap.put("timestamp" , ""+timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "the user is no longer admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkIfAlreadyExists(ModelUser modelUser, HolderParticipantsAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String hisRole = ""+snapshot.child("role").getValue();
                    holder.statusTv.setText(hisRole);

                }
                else
                {
                    holder.statusTv.setText("");

                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantsAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv, emailTv, statusTv;


        public HolderParticipantsAdd(@NonNull  View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);


        }
    }
}
