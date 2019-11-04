package live.qubbe.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import live.qubbe.android.BuildConfig;
import live.qubbe.android.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Constant {

    private File localFile;
    private Context context;

    public Constant(Context context) {
        this.context = context;
    }

    public File locationFolder() {
        localFile = new File(Environment.getExternalStorageDirectory() + "/"
                + context.getResources().getString(R.string.app_name));

        if (!localFile.exists()) {
            //Create Folder From Path
            localFile.mkdir();
        }

        return localFile;
    }

    public void shareApplication() {
        ApplicationInfo app = context.getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);
        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");

        // Append file and send Intent
        File originalApk = new File(filePath);

        try {
            //Make new directory in new location
            File tempFile = new File(context.getExternalCacheDir() + "/ExtractedApk");
            //If directory doesn't exists create new
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            //Get application's name and convert to lowercase
            tempFile = new File(tempFile.getPath() + "/" + context.getString(app.labelRes)
                    .replace(" ", "").toLowerCase() + ".apk");
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            //Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
            //Open share dialog

            Uri newUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                newUri = FileProvider.getUriForFile(context.getApplicationContext(),
                        BuildConfig.APPLICATION_ID + ".provider", tempFile);
            } else {
                newUri = Uri.fromFile(tempFile);
            }

            intent.putExtra(Intent.EXTRA_STREAM, newUri);
            context.startActivity(Intent.createChooser(intent, context.getResources().
                    getString(R.string.share_app_using)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
