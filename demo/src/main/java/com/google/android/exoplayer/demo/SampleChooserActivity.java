/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.demo.Samples.Sample;
import com.google.android.exoplayer.demo.player.DashRendererBuilder;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.demo.player.ExtractorRendererBuilder;
import com.google.android.exoplayer.demo.player.HlsRendererBuilder;
import com.google.android.exoplayer.demo.player.SmoothStreamingRendererBuilder;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.ApicFrame;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.DebugTextViewHelper;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;
import com.google.android.exoplayer.util.VerboseLogUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * An activity for selecting from a number of samples.
 */
public class SampleChooserActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_chooser_activity);
    final List<SampleGroup> sampleGroups = new ArrayList<>();
    SampleGroup group = new SampleGroup("YouTube DASH");
//    group.addAll(Samples.YOUTUBE_DASH_MP4);
//    group.addAll(Samples.YOUTUBE_DASH_WEBM);
//    sampleGroups.add(group);
//    group = new SampleGroup("Widevine DASH Policy Tests (GTS)");
//    group.addAll(Samples.WIDEVINE_GTS);
//    sampleGroups.add(group);
//    group = new SampleGroup("Widevine HDCP Capabilities Tests");
//    group.addAll(Samples.WIDEVINE_HDCP);
//    sampleGroups.add(group);
    group = new SampleGroup("Widevine DASH: MP4,H264");
    group.addAll(Samples.WIDEVINE_H264_MP4_CLEAR);
    group.addAll(Samples.WIDEVINE_H264_MP4_SECURE);
    sampleGroups.add(group);
//    group = new SampleGroup("Widevine DASH: WebM,VP9");
//    group.addAll(Samples.WIDEVINE_VP9_WEBM_CLEAR);
//    group.addAll(Samples.WIDEVINE_VP9_WEBM_SECURE);
//    sampleGroups.add(group);
//    group = new SampleGroup("Widevine DASH: MP4,H265");
//    group.addAll(Samples.WIDEVINE_H265_MP4_CLEAR);
//    group.addAll(Samples.WIDEVINE_H265_MP4_SECURE);
//    sampleGroups.add(group);
//    group = new SampleGroup("SmoothStreaming");
//    group.addAll(Samples.SMOOTHSTREAMING);
//    sampleGroups.add(group);
//    group = new SampleGroup("HLS");
//    group.addAll(Samples.HLS);
//    sampleGroups.add(group);
//    group = new SampleGroup("Misc");
//    group.addAll(Samples.MISC);
//    sampleGroups.add(group);
    ExpandableListView sampleList = (ExpandableListView) findViewById(R.id.sample_list);
    sampleList.setAdapter(new SampleAdapter(this, sampleGroups));
    sampleList.setOnChildClickListener(new OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
                                  int childPosition, long id) {
        onSampleSelected(sampleGroups.get(groupPosition).samples.get(childPosition));
        return true;
      }
    });
    sampleList.expandGroup(0);

    final FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.replace(R.id.player_container, new PlayerFragment());
    transaction.commit();
  }

  private void onSampleSelected(Sample sample) {
    final PlayerFragment playerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.player_container);
    playerFragment.onHidden();
    playerFragment.onShown(Uri.parse(sample.uri), sample.contentId, sample.type, sample.provider);
  }

  private static final class SampleAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<SampleGroup> sampleGroups;

    public SampleAdapter(Context context, List<SampleGroup> sampleGroups) {
      this.context = context;
      this.sampleGroups = sampleGroups;
    }

    @Override
    public Sample getChild(int groupPosition, int childPosition) {
      return getGroup(groupPosition).samples.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
        View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent,
            false);
      }
      ((TextView) view).setText(getChild(groupPosition, childPosition).name);
      return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return getGroup(groupPosition).samples.size();
    }

    @Override
    public SampleGroup getGroup(int groupPosition) {
      return sampleGroups.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
        ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = LayoutInflater.from(context).inflate(R.layout.sample_chooser_inline_header, parent,
            false);
      }
      ((TextView) view).setText(getGroup(groupPosition).title);
      return view;
    }

    @Override
    public int getGroupCount() {
      return sampleGroups.size();
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return true;
    }

  }

  private static final class SampleGroup {

    public final String title;
    public final List<Sample> samples;

    public SampleGroup(String title) {
      this.title = title;
      this.samples = new ArrayList<>();
    }

    public void addAll(Sample[] samples) {
      Collections.addAll(this.samples, samples);
    }

  }

  /**
   * the most part of code for the fragment copied from PlayerActivity
   */
  @SuppressLint("ValidFragment")
  public class PlayerFragment extends Fragment  implements SurfaceHolder.Callback, View.OnClickListener,
          DemoPlayer.Listener, DemoPlayer.CaptionListener, DemoPlayer.Id3MetadataListener,
          AudioCapabilitiesReceiver.Listener  {

    private static final String TAG = "PlayerFragment";
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;

    private final CookieManager defaultCookieManager;
    {
      defaultCookieManager = new CookieManager();
      defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private MediaController mediaController;
    private View debugRootView;
    private SurfaceView surfaceView;
    private TextView debugTextView;
    private TextView playerStateTextView;
    private SubtitleLayout subtitleLayout;
    private Button videoButton;
    private Button audioButton;
    private Button textButton;
    private Button retryButton;

    private DemoPlayer player;
    private DebugTextViewHelper debugViewHelper;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private boolean enableBackgroundAudio;

    private Uri contentUri;
    private int contentType;
    private String contentId;
    private String provider;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private FrameLayout videoContainer;

    public PlayerFragment() {
      // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      final View inflateView = inflater.inflate(R.layout.fragment_some, container, false);

      View root = inflateView.findViewById(R.id.root);
      root.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            toggleControlsVisibility();
          } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.performClick();
          }
          return true;
        }
      });
      root.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                  || keyCode == KeyEvent.KEYCODE_MENU) {
            return false;
          }
          return mediaController.dispatchKeyEvent(event);
        }
      });
      debugRootView = inflateView.findViewById(R.id.controls_root);

      videoContainer = (FrameLayout) inflateView.findViewById(R.id.channel_video_container);
      surfaceView = (SurfaceView) inflateView.findViewById(R.id.surface_view);
      surfaceView.getHolder().addCallback(this);
      debugTextView = (TextView) inflateView.findViewById(R.id.debug_text_view);

      playerStateTextView = (TextView) inflateView.findViewById(R.id.player_state_view);
      subtitleLayout = (SubtitleLayout) inflateView.findViewById(R.id.subtitles);

      mediaController = new KeyCompatibleMediaController(getActivity());
      mediaController.setAnchorView(root);
      retryButton = (Button) inflateView.findViewById(R.id.retry_button);
      retryButton.setOnClickListener(this);
      videoButton = (Button) inflateView.findViewById(R.id.video_controls);
      audioButton = (Button) inflateView.findViewById(R.id.audio_controls);
      textButton = (Button) inflateView.findViewById(R.id.text_controls);

      CookieHandler currentHandler = CookieHandler.getDefault();
      if (currentHandler != defaultCookieManager) {
        CookieHandler.setDefault(defaultCookieManager);
      }

      audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this.getContext(), this);
      audioCapabilitiesReceiver.register();

      inflateView.findViewById(R.id.video_controls).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          PlayerFragment.this.showVideoPopup(v);
        }
      });
      inflateView.findViewById(R.id.audio_controls).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          PlayerFragment.this.showAudioPopup(v);
        }
      });
      inflateView.findViewById(R.id.text_controls).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          PlayerFragment.this.showTextPopup(v);
        }
      });
      inflateView.findViewById(R.id.verbose_log_controls).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          PlayerFragment.this.showVerboseLogPopup(v);
        }
      });
      return inflateView;
    }

    public Context getContext() {
      return getActivity();
    }

    @Override
    public void onPause() {
      super.onPause();
      onHidden();
    }

    private void onShown(Uri parse, String contentId, int type, String provider) {
      contentUri = parse;
      contentType = type;
      this.contentId = contentId;
      this.provider = provider;

      configureSubtitleView();
      if (player == null) {
        if (!maybeRequestPermission()) {
          preparePlayer(true);
        }
      } else {
        player.setBackgrounded(false);
      }
    }

    private void onHidden() {
        releasePlayer();
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      audioCapabilitiesReceiver.unregister();
      releasePlayer();
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {
      if (view == retryButton) {
        preparePlayer(true);
      }
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
      if (player == null) {
        return;
      }
      boolean backgrounded = player.getBackgrounded();
      boolean playWhenReady = player.getPlayWhenReady();
      releasePlayer();
      preparePlayer(playWhenReady);
      player.setBackgrounded(backgrounded);
    }

    // Permission request listener method

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        preparePlayer(true);
      } else {
        Toast.makeText(this.getContext().getApplicationContext(), R.string.storage_permission_denied,
                Toast.LENGTH_LONG).show();
        ((PlayerActivity)this.getContext()).finish();
      }
    }

    // Permission management methods

    /**
     * Checks whether it is necessary to ask for permission to read storage. If necessary, it also
     * requests permission.
     *
     * @return true if a permission request is made. False if it is not necessary.
     */
    @TargetApi(23)
    private boolean maybeRequestPermission() {
      if (requiresPermission(contentUri)) {
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        return true;
      } else {
        return false;
      }
    }

    @TargetApi(23)
    private boolean requiresPermission(Uri uri) {
      return Util.SDK_INT >= 23
              && Util.isLocalFileUri(uri)
              && ((PlayerActivity)this.getContext()).checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
              != PackageManager.PERMISSION_GRANTED;
    }

    // Internal methods

    private DemoPlayer.RendererBuilder getRendererBuilder() {
      String userAgent = Util.getUserAgent(getContext(), "ExoPlayerDemo");
      Log.d(TAG, "prepare, uri = " + contentUri.toString());
      switch (contentType) {
        case Util.TYPE_SS:
          return new SmoothStreamingRendererBuilder(getContext(), userAgent, contentUri.toString(),
                  new SmoothStreamingTestMediaDrmCallback());
        case Util.TYPE_DASH:
          return new DashRendererBuilder(getContext(), userAgent, contentUri.toString(),
                  new WidevineTestMediaDrmCallback(contentId, provider));
        case Util.TYPE_HLS:
          return new HlsRendererBuilder(getContext(), userAgent, contentUri.toString());
        case Util.TYPE_OTHER:
          return new ExtractorRendererBuilder(getContext(), userAgent, contentUri);
        default:
          throw new IllegalStateException("Unsupported type: " + contentType);
      }
    }

    private void preparePlayer(boolean playWhenReady) {
      if (player == null) {
        player = new DemoPlayer(getRendererBuilder());
        player.addListener(this);
        player.setCaptionListener(this);
        player.setMetadataListener(this);
        player.seekTo(playerPosition);
        playerNeedsPrepare = true;
        mediaController.setMediaPlayer(player.getPlayerControl());
        mediaController.setEnabled(true);
        eventLogger = new EventLogger();
        eventLogger.startSession();
        player.addListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.setInternalErrorListener(eventLogger);
        debugViewHelper = new DebugTextViewHelper(player, debugTextView);
        debugViewHelper.start();
      }
      if (playerNeedsPrepare) {
        player.prepare();
        playerNeedsPrepare = false;
        updateButtonVisibilities();
      }
      player.setSurface(surfaceView.getHolder().getSurface());
      player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
      if (player != null) {
        debugViewHelper.stop();
        debugViewHelper = null;
        playerPosition = player.getCurrentPosition();
        player.release();
        player = null;
        eventLogger.endSession();
        eventLogger = null;
      }
    }

    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
      if (playbackState == ExoPlayer.STATE_ENDED) {
        showControls();
      }
      String text = "playWhenReady=" + playWhenReady + ", playbackState=";
      switch(playbackState) {
        case ExoPlayer.STATE_BUFFERING:
          text += "buffering";
          break;
        case ExoPlayer.STATE_ENDED:
          text += "ended";
          break;
        case ExoPlayer.STATE_IDLE:
          text += "idle";
          break;
        case ExoPlayer.STATE_PREPARING:
          text += "preparing";
          break;
        case ExoPlayer.STATE_READY:
          text += "ready";
          break;
        default:
          text += "unknown";
          break;
      }
      playerStateTextView.setText(text);
      updateButtonVisibilities();
    }

    @Override
    public void onError(Exception e) {
      String errorString = null;
      if (e instanceof UnsupportedDrmException) {
        // Special case DRM failures.
        UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
        errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
      } else if (e instanceof ExoPlaybackException
              && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
        // Special case for decoder initialization failures.
        MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
        if (decoderInitializationException.decoderName == null) {
          if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
            errorString = getString(R.string.error_querying_decoders);
          } else if (decoderInitializationException.secureDecoderRequired) {
            errorString = getString(R.string.error_no_secure_decoder,
                    decoderInitializationException.mimeType);
          } else {
            errorString = getString(R.string.error_no_decoder,
                    decoderInitializationException.mimeType);
          }
        } else {
          errorString = getString(R.string.error_instantiating_decoder,
                  decoderInitializationException.decoderName);
        }
      }
      if (errorString != null) {
        Toast.makeText(getContext().getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
      }
      playerNeedsPrepare = true;
      updateButtonVisibilities();
      showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
    }

    // User controls

    private void updateButtonVisibilities() {
      retryButton.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
      videoButton.setVisibility(haveTracks(DemoPlayer.TYPE_VIDEO) ? View.VISIBLE : View.GONE);
      audioButton.setVisibility(haveTracks(DemoPlayer.TYPE_AUDIO) ? View.VISIBLE : View.GONE);
      textButton.setVisibility(haveTracks(DemoPlayer.TYPE_TEXT) ? View.VISIBLE : View.GONE);
    }

    private boolean haveTracks(int type) {
      return player != null && player.getTrackCount(type) > 0;
    }

    public void showVideoPopup(View v) {
      PopupMenu popup = new PopupMenu(getContext(), v);
      configurePopupWithTracks(popup, null, DemoPlayer.TYPE_VIDEO);
      popup.show();
    }

    public void showAudioPopup(View v) {
      PopupMenu popup = new PopupMenu(getContext(), v);
      Menu menu = popup.getMenu();
      menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.enable_background_audio);
      final MenuItem backgroundAudioItem = menu.findItem(0);
      backgroundAudioItem.setCheckable(true);
      backgroundAudioItem.setChecked(enableBackgroundAudio);
      PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          if (item == backgroundAudioItem) {
            enableBackgroundAudio = !item.isChecked();
            return true;
          }
          return false;
        }
      };
      configurePopupWithTracks(popup, clickListener, DemoPlayer.TYPE_AUDIO);
      popup.show();
    }

    public void showTextPopup(View v) {
      PopupMenu popup = new PopupMenu(getContext(), v);
      configurePopupWithTracks(popup, null, DemoPlayer.TYPE_TEXT);
      popup.show();
    }

    public void showVerboseLogPopup(View v) {
      PopupMenu popup = new PopupMenu(getContext(), v);
      Menu menu = popup.getMenu();
      menu.add(Menu.NONE, 0, Menu.NONE, R.string.logging_normal);
      menu.add(Menu.NONE, 1, Menu.NONE, R.string.logging_verbose);
      menu.setGroupCheckable(Menu.NONE, true, true);
      menu.findItem((VerboseLogUtil.areAllTagsEnabled()) ? 1 : 0).setChecked(true);
      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          if (item.getItemId() == 0) {
            VerboseLogUtil.setEnableAllTags(false);
          } else {
            VerboseLogUtil.setEnableAllTags(true);
          }
          return true;
        }
      });
      popup.show();
    }

    private void configurePopupWithTracks(PopupMenu popup,
                                          final PopupMenu.OnMenuItemClickListener customActionClickListener,
                                          final int trackType) {
      if (player == null) {
        return;
      }
      int trackCount = player.getTrackCount(trackType);
      if (trackCount == 0) {
        return;
      }
      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          return (customActionClickListener != null
                  && customActionClickListener.onMenuItemClick(item))
                  || onTrackItemClick(item, trackType);
        }
      });
      Menu menu = popup.getMenu();
      // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0).
      menu.add(MENU_GROUP_TRACKS, DemoPlayer.TRACK_DISABLED + ID_OFFSET, Menu.NONE, R.string.off);
      for (int i = 0; i < trackCount; i++) {
        menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
                buildTrackName(player.getTrackFormat(trackType, i)));
      }
      menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
      menu.findItem(player.getSelectedTrack(trackType) + ID_OFFSET).setChecked(true);
    }

    private String buildTrackName(MediaFormat format) {
      if (format.adaptive) {
        return "auto";
      }
      String trackName;
      if (MimeTypes.isVideo(format.mimeType)) {
        trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                buildBitrateString(format)), buildTrackIdString(format));
      } else if (MimeTypes.isAudio(format.mimeType)) {
        trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                        buildAudioPropertyString(format)), buildBitrateString(format)),
                buildTrackIdString(format));
      } else {
        trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                buildBitrateString(format)), buildTrackIdString(format));
      }
      return trackName.length() == 0 ? "unknown" : trackName;
    }

    private String buildResolutionString(MediaFormat format) {
      return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE
              ? "" : format.width + "x" + format.height;
    }

    private String buildAudioPropertyString(MediaFormat format) {
      return format.channelCount == MediaFormat.NO_VALUE || format.sampleRate == MediaFormat.NO_VALUE
              ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private String buildLanguageString(MediaFormat format) {
      return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
              : format.language;
    }

    private String buildBitrateString(MediaFormat format) {
      return format.bitrate == MediaFormat.NO_VALUE ? ""
              : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private String joinWithSeparator(String first, String second) {
      return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private String buildTrackIdString(MediaFormat format) {
      return format.trackId == null ? "" : " (" + format.trackId + ")";
    }

    private boolean onTrackItemClick(MenuItem item, int type) {
      if (player == null || item.getGroupId() != MENU_GROUP_TRACKS) {
        return false;
      }
      player.setSelectedTrack(type, item.getItemId() - ID_OFFSET);
      return true;
    }

    private void toggleControlsVisibility()  {
      if (mediaController.isShowing()) {
        mediaController.hide();
        debugRootView.setVisibility(View.GONE);
      } else {
        showControls();
      }
    }

    private void showControls() {
      mediaController.show(0);
      debugRootView.setVisibility(View.VISIBLE);
    }

    // DemoPlayer.CaptionListener implementation

    @Override
    public void onCues(List<Cue> cues) {
      subtitleLayout.setCues(cues);
    }

    // DemoPlayer.MetadataListener implementation

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
      for (Id3Frame id3Frame : id3Frames) {
        if (id3Frame instanceof TxxxFrame) {
          TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
          Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                  txxxFrame.description, txxxFrame.value));
        } else if (id3Frame instanceof PrivFrame) {
          PrivFrame privFrame = (PrivFrame) id3Frame;
          Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
        } else if (id3Frame instanceof GeobFrame) {
          GeobFrame geobFrame = (GeobFrame) id3Frame;
          Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                  geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
        } else if (id3Frame instanceof ApicFrame) {
          ApicFrame apicFrame = (ApicFrame) id3Frame;
          Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, description=%s",
                  apicFrame.id, apicFrame.mimeType, apicFrame.description));
        } else if (id3Frame instanceof TextInformationFrame) {
          TextInformationFrame textInformationFrame = (TextInformationFrame) id3Frame;
          Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s", textInformationFrame.id,
                  textInformationFrame.description));
        } else {
          Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
        }
      }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      if (player != null) {
        player.setSurface(holder.getSurface());
      }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      if (player != null) {
        player.blockingClearSurface();
      }
    }

    private void configureSubtitleView() {
      CaptionStyleCompat style;
      float fontScale;
      if (Util.SDK_INT >= 19) {
        style = getUserCaptionStyleV19();
        fontScale = getUserCaptionFontScaleV19();
      } else {
        style = CaptionStyleCompat.DEFAULT;
        fontScale = 1.0f;
      }
      subtitleLayout.setStyle(style);
      subtitleLayout.setFractionalTextSize(SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION * fontScale);

    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
      CaptioningManager captioningManager =
              (CaptioningManager) getContext().getSystemService(Context.CAPTIONING_SERVICE);
      return captioningManager.getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
      CaptioningManager captioningManager =
              (CaptioningManager) getContext().getSystemService(Context.CAPTIONING_SERVICE);
      return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    /**
     * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
     * extension.
     *
     * @param uri The {@link Uri} of the media.
     * @param fileExtension An overriding file extension.
     * @return The inferred type.
     */
    private int inferContentType(Uri uri, String fileExtension) {
      String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
              : uri.getLastPathSegment();
      return Util.inferContentType(lastPathSegment);
    }

    private final class KeyCompatibleMediaController extends MediaController {

      private MediaController.MediaPlayerControl playerControl;

      public KeyCompatibleMediaController(Context context) {
        super(context);
      }

      @Override
      public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
        super.setMediaPlayer(playerControl);
        this.playerControl = playerControl;
      }

      @Override
      public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (playerControl.canSeekForward() && (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
          if (event.getAction() == KeyEvent.ACTION_DOWN) {
            playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
            show();
          }
          return true;
        } else if (playerControl.canSeekBackward() && (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)) {
          if (event.getAction() == KeyEvent.ACTION_DOWN) {
            playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
            show();
          }
          return true;
        }
        return super.dispatchKeyEvent(event);
      }
    }

  }

}
