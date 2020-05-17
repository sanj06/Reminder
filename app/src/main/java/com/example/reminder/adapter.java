package com.example.reminder;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.myViewHolder> {

    Context mcontext;
    List<item> mdata;
    ArrayList<String> keylist;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public adapter(Context mcontext, List<item> mdata, ArrayList<String> keylist) {
        this.mcontext = mcontext;
        this.mdata = mdata;
        this.keylist = keylist;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mcontext);
        View v = inflater.inflate(R.layout.card_item,parent,false);
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, final int position) {

        holder.r_date.setText(mdata.get(position).getDate());
        holder.r_day.setText(mdata.get(position).getDay());
        holder.r_month.setText(mdata.get(position).getMonth());
        holder.r_desc.setText(mdata.get(position).getDesc());
        holder.r_time.setText(mdata.get(position).getTime());

        final String keyval = mdata.get(position).getKey();

        holder.r_desc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("LONG CLICK");
                AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
                builder.setMessage("Do you want to delete this reminder?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                removeItem(keyval, position);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();


                //removeItem(position);
                return false;
            }
        });
        }





    @Override
    public int getItemCount() {
        return mdata.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        TextView r_date, r_day, r_month, r_desc, r_time;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            r_date = itemView.findViewById(R.id.r_date);
            r_day = itemView.findViewById(R.id.r_day);
            r_month = itemView.findViewById(R.id.r_month);
            r_desc = itemView.findViewById(R.id.r_desc);
            r_time = itemView.findViewById(R.id.r_time);

        }

    }

    private void removeItem(String keyval, int position) {
        databaseReference.child(keyval).removeValue();
        mdata.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mdata.size());
    }
}

