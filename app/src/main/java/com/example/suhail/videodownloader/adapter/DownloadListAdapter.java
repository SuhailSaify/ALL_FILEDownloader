package com.example.suhail.videodownloader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.example.suhail.videodownloader.Model.Urls;
import com.example.suhail.videodownloader.R;
import com.example.suhail.videodownloader.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Suhail on 4/4/2018.
 */

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {

    String dirPath;

    String name;
    ArrayList<Urls> data = new ArrayList<>();
    Context context;

    public DownloadListAdapter(ArrayList<Urls> data, Context context, String dirPath) {
        this.data = data;
        this.context = context;
        this.dirPath = dirPath;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_download, parent, false);


        return new DownloadListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        name = URLUtil.guessFileName(data.get(position).getUrl(), null, null);

        holder.filename.setText(name);

        holder.buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (Status.RUNNING == PRDownloader.getStatus(data.get(position).getId())) {
                    PRDownloader.pause(data.get(position).getId());
                    return;
                }

                holder.buttonOne.setEnabled(false);

                holder.progressBarOne.setIndeterminate(true);
                holder.progressBarOne.getIndeterminateDrawable().setColorFilter(
                        Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

                if (Status.PAUSED == PRDownloader.getStatus(data.get(position).getId())) {
                    PRDownloader.resume(data.get(position).getId());
                    return;
                }


                data.get(position).setId(PRDownloader.download(data.get(position).getUrl(), dirPath, name)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                holder.progressBarOne.setIndeterminate(false);
                                holder.buttonOne.setEnabled(true);
                                holder.buttonOne.setText(R.string.pause);

                                holder.buttonCancelOne.setEnabled(true);
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                holder.buttonOne.setText(R.string.resume);
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                holder.buttonOne.setText(R.string.start);
                                holder.buttonCancelOne.setEnabled(false);
                                holder.progressBarOne.setProgress(0);

                                holder.textViewProgressOne.setText("");
                                data.get(position).setId(0);
                                holder.progressBarOne.setIndeterminate(false);
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                holder.progressBarOne.setProgress((int) progressPercent);

                                holder.textViewProgressOne.setText(Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                holder.progressBarOne.setIndeterminate(false);
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                holder.buttonOne.setEnabled(false);
                                holder.buttonCancelOne.setEnabled(false);
                                holder.buttonOne.setText(R.string.completed);
                            }

                            @Override
                            public void onError(Error error) {
                                holder.buttonOne.setText(R.string.start);
                                Toast.makeText(context, context.getString(R.string.some_error_occurred) + " " + "1", Toast.LENGTH_SHORT).show();
                                holder.textViewProgressOne.setText("");
                                holder.progressBarOne.setProgress(0);
                                data.get(position).setId(0);
                                holder.buttonCancelOne.setEnabled(false);
                                holder.progressBarOne.setIndeterminate(false);
                                holder.buttonOne.setEnabled(true);
                            }
                        }));
            }
        });

        holder.buttonCancelOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(data.get(position).getId());
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button buttonOne, buttonCancelOne;

        TextView textViewProgressOne, filename;

        ProgressBar progressBarOne;


        public ViewHolder(View itemView) {
            super(itemView);

            buttonCancelOne = itemView.findViewById(R.id.buttonCancel_download);
            buttonOne = itemView.findViewById(R.id.buttonone_download);
            progressBarOne = itemView.findViewById(R.id.progressBar_download);
            textViewProgressOne = itemView.findViewById(R.id.textViewProgress_download);
            filename = itemView.findViewById(R.id.filename_download);
        }
    }
}
