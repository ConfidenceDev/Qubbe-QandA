package live.qubbe.android.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FileMethods {

    private Context context;

    public FileMethods(Context context){
        this.context = context;
    }

    public File createTempFile(File folder, String ext) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "QUBBE" + timeStamp + "_";
        return File.createTempFile(imageFileName, ext, folder);
    }

    public String createTempName(String ext) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "QUBBE" + timeStamp + "_";
    }

    private File createVideoFile(File folder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "QUBBE" + timeStamp + "_";
        //currentPicture = imageFileName;
        return File.createTempFile(imageFileName, ".mp4", folder);
    }

    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageFile.getAbsolutePath();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = 0;
            if (result != null) {
                cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public String getFileSize(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = String.valueOf(cursor.getInt(cursor.getColumnIndex(OpenableColumns.SIZE)) / (1024 * 1024));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = 0;
            if (result != null) {
                cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public static String getFileType(Context context, Uri uri) {
        String ext;

        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            ext = mimeTypeMap.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            ext = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(Objects.requireNonNull(uri.getPath()))).toString());
        }
        return ext;
    }

    private boolean animateExit(RecyclerView.ViewHolder holder) {
        holder.itemView.clearAnimation();

        holder.itemView.animate()
                .alpha(0)
                .setInterpolator(new AccelerateInterpolator(2.f))
                .setDuration(350)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //dispatchRemoveFinished(holder);
                    }
                })
                .start();

        return false;
    }

    private boolean animateEnter(RecyclerView.ViewHolder holder) {
        holder.itemView.clearAnimation();

        final int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        holder.itemView.setTranslationY(screenHeight);
        holder.itemView.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(650)
                .setStartDelay(450 + holder.getLayoutPosition() * 75)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //dispatchAddFinished(holder);
                    }
                })
                .start();

        return false;
    }

    public File tempMedia(String sourcePath, String targetPath){
        try {
            InputStream in = new FileInputStream(sourcePath);
            OutputStream out = new FileOutputStream(targetPath);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return new File(targetPath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startInstagram(String mediaPath) {
        final String INSTAGRAM_PACKAGE_NAME = "com.instagram.android";

        /*try {
            File media = new File(mediaPath);
            Uri uri = Uri.fromFile(media);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("video/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setPackage(INSTAGRAM_PACKAGE_NAME);
            startActivity(share);

            // startActivity(Intent.createChooser(share, R.string.chooserTitle));
        } catch (Exception e) { // Instagram package not present on device
            Toast.makeText(getActivity(), R.string.instagramRequirement, Toast.LENGTH_SHORT)
                    .show();

            try { // Prompt to install Instagram via Google Play
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + INSTAGRAM_PACKAGE_NAME)));
            } catch (ActivityNotFoundException anfe) { // Prompt to install Instagram via browser
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id="
                                + INSTAGRAM_PACKAGE_NAME)));
            }
        }*/
    }

}
