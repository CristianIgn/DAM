package com.denis.auzimusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    CardView playBtn;
    CardView previousBtn;
    CardView nextBtn;
    ImageView shuffleBtn;
    ImageView playBtnIcon;
    SeekBar seekBar;
    TextView endTime;
    TextView songTitle;
    TextView beginTime;
    LinearLayout playlistUi;
    boolean bPlaying = false;
    boolean bShuffle = false;
    ArrayList<Uri> playList = new ArrayList<Uri>();
    int currentIndex = 0;
    CardView playlistView;
    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing
        final ImageView placeholderBtn = findViewById(R.id.songPlaceholder);
        songTitle = findViewById(R.id.songTitle);
        beginTime = findViewById(R.id.timeBegin);
        endTime = findViewById(R.id.timeEnd);
        seekBar = findViewById(R.id.seekBar);
        playBtn = findViewById(R.id.playButton);
        previousBtn = findViewById(R.id.previousButton);
        nextBtn = findViewById(R.id.nextButton);
        shuffleBtn = findViewById(R.id.shuffleButton);
        playBtnIcon = findViewById(R.id.playBtnIcon);
        playlistUi = findViewById(R.id.playlistUi);
        playlistView = findViewById(R.id.playlistView);


        // fires when the button to pick a song is pressed
        placeholderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mediaPlayer != null)
                {
                    if(mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    playList.clear();
                    playlistUi.removeAllViews();
                    bPlaying = false;
                    playBtnIcon.setImageResource(R.drawable.ic_play);
                    bShuffle = false;
                    currentIndex = 0;
                }
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(intent, 1);
                }
                catch(Exception exception)
                {
                    Toast.makeText(MainActivity.this, "Unable to open file manager", Toast.LENGTH_SHORT).show();
                }
            }

        });

        // fires when the button play button is pressed
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mediaPlayer == null)
                    return;
                if(bPlaying && mediaPlayer.isPlaying())
                {
                    // stops the music & changes the button icon
                    playBtnIcon.setImageResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else if (!bPlaying)
                {
                    // starts the music & changes the button icon
                    playBtnIcon.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
                bPlaying = !bPlaying;

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                playNextSong();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mediaPlayer == null)
                    return;
                if(currentIndex - 1 < 0)
                    mediaPlayer.seekTo(0);
                else
                {
                    mediaPlayer.reset();

                    try {
                        mediaPlayer.setDataSource(getApplicationContext(), playList.get(--currentIndex));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        setupUi();
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mediaPlayer == null)
                    return;
                if(bShuffle)
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                else if (!bShuffle)
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_active);

                bShuffle = !bShuffle;

            }
        });
        // fires when the seekbar has changed
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                // saving new location
                prog = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying() && mediaPlayer != null) {
                    mediaPlayer.seekTo(prog * 1000);
                }
            }
        });
    }




    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != 1 || resultCode != RESULT_OK || data == null)
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

        if(data.getClipData() == null)
        {
            playList.add(data.getData());
            previousBtn.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            shuffleBtn.setVisibility(View.GONE);
            playlistView.setVisibility(View.GONE);
        }

        else
        {
            previousBtn.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);
            shuffleBtn.setVisibility(View.VISIBLE);
            playlistView.setVisibility(View.VISIBLE);
            for(int i = 0; i < data.getClipData().getItemCount(); i++)
            {
                Uri songUri = data.getClipData().getItemAt(i).getUri();
                playList.add(songUri);

                CardView cardView = new CardView(this);
                TextView textView = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(950, 120);
                layoutParams.setMargins(30, 5, 1, 0);
                cardView.setLayoutParams(layoutParams);
                cardView.setCardBackgroundColor(Color.parseColor("#34252F"));
                cardView.setRadius(45);
                cardView.setCardElevation(16);

                textView.setTextSize(20);
                LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(30, 30, 1, 30);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);

                metaRetriever.setDataSource(getApplicationContext(), songUri);
                String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if(title == null || artist == null)
                    textView.setText(Paths.get(songUri.getPath()).getFileName().toString());
                else
                    textView.setText(artist + " - " + title);

                textView.setTextColor(Color.parseColor("#FFFFFF"));
                cardView.addView(textView);
                cardView.setTag(i);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        if(mediaPlayer == null)
                            return;

                        mediaPlayer.reset();
                        try {
                            currentIndex = (Integer)view.getTag();
                            mediaPlayer.setDataSource(getApplicationContext(), playList.get(currentIndex));
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playBtnIcon.setImageResource(R.drawable.ic_pause);
                            bPlaying = true;
                            setupUi();
                        }
                        catch (Exception ex)
                        {

                        }
                    }
                });


                playlistUi.addView(cardView);
            }
        }

        playSong();


    }

    public void playSong()
    {
        // initializing the mediaplayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(getApplicationContext(), playList.get(currentIndex));
            mediaPlayer.prepare();

            // disposes mediaplayer when it finishes the song
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer player)
                {
                    playBtnIcon.setImageResource(R.drawable.ic_play);
                    bPlaying = false;
                    playNextSong();
                }
            });

            setupUi();



        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }


    }

    public int getSong(ArrayList<Uri> list)
    {
        if(bShuffle)
        {
            int randomSong = (new Random()).nextInt(((list.size() - 1) - 0) + 1) + 0;
            if(randomSong == currentIndex)
                return getSong(list);
            currentIndex = randomSong;
            return randomSong;
        }
        else
            return ++currentIndex;

    }

    public void playNextSong()
    {
        if(mediaPlayer == null)
            return;
        if(currentIndex + 1 >= playList.size() && !bShuffle)
            mediaPlayer.seekTo(0);
        else
        {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getApplicationContext(), playList.get(getSong(playList)));
                mediaPlayer.prepare();
                mediaPlayer.start();
                playBtnIcon.setImageResource(R.drawable.ic_pause);
                bPlaying = true;
                setupUi();
            }
            catch (Exception ex)
            {

            }
        }
    }
    public void setupUi()
    {
        //parsing duration
        long durationSeconds = TimeUnit.SECONDS.convert(mediaPlayer.getDuration(), TimeUnit.MILLISECONDS);

        endTime.setText(getTime(mediaPlayer.getDuration()));
        seekBar.setMax((int)durationSeconds);

        // retrieving metadata from the file

        metaRetriever.setDataSource(getApplicationContext(), playList.get(currentIndex));
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if(title == null || artist == null)
            songTitle.setText(Paths.get(playList.get(currentIndex).getPath()).getFileName().toString());
        else
            songTitle.setText(artist + " - " + title);

        // animating seekbar & current point in song on another thread
        Handler handler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(mediaPlayer != null){
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);

                    long playedTime = TimeUnit.SECONDS.convert(mediaPlayer.getCurrentPosition(), TimeUnit.SECONDS);
                    beginTime.setText(getTime(mediaPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private String getTime(long playerPosition)
    {
        int minutes = (int) ((playerPosition % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((playerPosition % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        return String.format("%02d:%02d", minutes, seconds);
    }
}