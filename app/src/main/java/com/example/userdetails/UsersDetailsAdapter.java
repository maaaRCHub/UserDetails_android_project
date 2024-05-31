package com.example.userdetails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.zip.Inflater;

public class UsersDetailsAdapter extends RecyclerView.Adapter<UsersDetailsAdapter.ViewHolder> {

    Context context;
    DBHelper dbHelper;
    List<DBUsersDetails> dbUsersDetailsList;

    public UsersDetailsAdapter(Context context, List<DBUsersDetails> dbUsersDetailsList) {
        this.context = context;
        this.dbUsersDetailsList = dbUsersDetailsList;
        dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_details_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBUsersDetails dbUsersDetails = dbUsersDetailsList.get(position);

        holder.id.setText(String.valueOf(dbUsersDetails.getId()));
        holder.name.setText(dbUsersDetails.getName());
        holder.email.setText(dbUsersDetails.getEmail());
        holder.address.setText(dbUsersDetails.getAddress());
        holder.phone.setText(dbUsersDetails.getPhone());

        String photoPath = dbUsersDetails.getPhoto();
        Log.d("UsersDetailsAdapter", "Photo path: " + photoPath);
//        if (photoPath != null && !photoPath.equals("NULL"))

        if (photoPath != null && !photoPath.equals("NULL")){
            holder.btnCamera.setVisibility(View.GONE);
            holder.btnGallery.setVisibility(View.VISIBLE);

            Bitmap bitmap = loadImageFromStorage(photoPath, "user" + dbUsersDetails.getId());

            if (bitmap != null) {
                holder.btnGallery.setImageBitmap(bitmap);
                holder.btnGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImageDialog(bitmap, dbUsersDetails);
                    }
                });
            } else {
                holder.btnGallery.setImageResource(R.drawable.default_image); // Set a default image if loading fails
                Log.d("UsersDetailsAdapter", "Failed to load image from path: " + photoPath);
            }
        } else {
            holder.btnCamera.setVisibility(View.VISIBLE);
            holder.btnGallery.setVisibility(View.GONE);
            holder.btnGallery.setImageBitmap(null); // To remove the previous image from the ImageView of the layout
        }


        holder.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CameraActivity.class);
                intent.putExtra("id", dbUsersDetails.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dbUsersDetailsList.size();
    }

    private Bitmap loadImageFromStorage(String path, String name) {
        try {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                return BitmapFactory.decodeStream(new FileInputStream(imgFile));
            } else {
                Log.d("UsersDetailsAdapter", "Image file does not exist: " + path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("UsersDetailsAdapter", "File not found: " + path);
        }
        return null;
    }


    private void showImageDialog(Bitmap bitmap, DBUsersDetails dbUsersDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        View dialogView = LayoutInflater.from(context).inflate(R.layout.image_dialog_layout, null);
        PhotoView imageView = dialogView.findViewById(R.id.dialogImageView);


        ImageView retakeButton = dialogView.findViewById(R.id.editImageView);
        ImageView deleteButton = dialogView.findViewById(R.id.deleteImageView);

        imageView.setImageBitmap(bitmap);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create(); // Create the dialog

        // Set OnClickListener for retake button
        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle retake button click
                Intent intent = new Intent(context, CameraActivity.class);
                intent.putExtra("id", dbUsersDetails.getId());
                context.startActivity(intent);
                alertDialog.dismiss(); // Dismiss the dialog after retaking the image
            }
        });

        // Set OnClickListener for delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle delete button click
                File file = new File(dbUsersDetails.getPhoto());
                if (file.exists() && file.delete()) {
                    Log.d("UsersDetailsAdapter", "Image file deleted: " + dbUsersDetails.getPhoto());
                } else {
                    Log.d("UsersDetailsAdapter", "Failed to delete image file: " + dbUsersDetails.getPhoto());
                }

                dbUsersDetails.setPhoto("NULL");
                dbHelper.updatePhoto(dbUsersDetails.getId(), "NULL");
                notifyDataSetChanged();
                alertDialog.dismiss(); // Dismiss the dialog after deleting the image
            }
        });

        // Set the background of the alert dialog to transparent
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show(); // Show the dialog
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, name, email, address, phone;
        ImageView btnCamera, btnGallery;
        LinearLayout btnLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            address = itemView.findViewById(R.id.address);
            phone = itemView.findViewById(R.id.phone);
            btnGallery = itemView.findViewById(R.id.imageView_gallery);
            btnCamera = itemView.findViewById(R.id.imageView_camera);
            btnLocation = itemView.findViewById(R.id.lLayout_location);
        }
    }
}
