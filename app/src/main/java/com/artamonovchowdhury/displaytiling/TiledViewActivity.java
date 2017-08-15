package com.artamonovchowdhury.displaytiling;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class TiledViewActivity extends AppCompatActivity {

    public final static String EXTRA_BITMAP = "com.artamonovchowdhury.displaytiling.extra.BITMAP";
    public final static String EXTRA_DIRECTION = "com.artamonovchowdhury.displaytiling.extra.DIRECTION";
    public final static String EXTRA_ISMASTER = "com.artamonovchowdhury.displaytiling.extra.ISMASTER";

    ImageView imgVw;
    Bitmap bmp;
    String dir;
    boolean isMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiled_view);

        imgVw = (ImageView) findViewById(R.id.tiledImageView);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra(EXTRA_BITMAP);
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        if (intent.hasExtra(EXTRA_DIRECTION)) {
            dir = intent.getStringExtra(EXTRA_DIRECTION);
        }

        isMaster = intent.getBooleanExtra(EXTRA_ISMASTER, true);
        displayImage();

    }

    private void displayImage() {
        imgVw.setImageBitmap(bmp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tiled_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
