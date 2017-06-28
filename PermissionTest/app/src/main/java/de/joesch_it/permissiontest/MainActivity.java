package de.joesch_it.permissiontest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = " ##### " + MainActivity.class.getSimpleName() + " #####";
    private static final String PREF_FILE = "de.joesch-it.permissiontest.PREFERENCE_FILE";
    private static final int PERMISSION_REQUEST_CODE_CALLBACK = 100;
    private static final int PERMISSION_REQUEST_CODE_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private TextView txtPermissions;
    private SharedPreferences mSharedPreferences;
    private boolean sentToSettings = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);

        txtPermissions = (TextView) findViewById(R.id.txtPermissions);
        Button btnCheckPermissions = (Button) findViewById(R.id.btnCheckPermissions);

        assert btnCheckPermissions != null;
        btnCheckPermissions.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {

                    // no permission granted yet (after clicking the button)

                    Log.v(TAG, "01");


                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[2])) {

                        // show permission rationale (Gründe, Begründung)

                        Log.v(TAG, "02");

                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("1. Need Multiple Permissions");
                        builder.setMessage("This app needs Camera and Location permissions.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();

                    } else if (mSharedPreferences.getBoolean(permissionsRequired[0], false)) {

                        // permissionsRequired[0] = CAMERA permission still needed

                        Log.v(TAG, "03: permissionsRequired[0] = " + String.valueOf(mSharedPreferences.getBoolean(permissionsRequired[0], false)));

                        // Previously Permission Request was cancelled with 'Dont Ask Again',
                        // Redirect to Settings after showing Information about why you need the permission

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("2. Need Multiple Permissions");
                        builder.setMessage("This app needs Camera and Location permissions.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                sentToSettings = true;
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, PERMISSION_REQUEST_CODE_SETTING);
                                Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera and Location", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();

                    } else {

                        Log.v(TAG, "04");

                        //just request the permission
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);

                    }

                    txtPermissions.setText(R.string.permissions_required);

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(permissionsRequired[0], true);
                    editor.apply();
                } else {

                    Log.v(TAG, "05");
                    //You already have the permission, just go ahead.
                    proceedAfterPermission();
                }
            }
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.v(TAG, "06 onRequestPermissionsResult() called");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_CALLBACK) {

            Log.v(TAG, "07");

            //check if all permissions are granted

            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {

                    Log.v(TAG, "08");
                    allgranted = true;

                } else {

                    Log.v(TAG, "09");
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {

                Log.v(TAG, "10");
                proceedAfterPermission();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[2])) {

                Log.v(TAG, "11");

                txtPermissions.setText(R.string.permissions_required);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("3. Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_REQUEST_CODE_CALLBACK);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override  public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else {

                Log.v(TAG, "12");
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "13");

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE_SETTING) {

            Log.v(TAG, "14");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "15");
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    private void proceedAfterPermission() {

        Log.v(TAG, "16");

        txtPermissions.setText(R.string.weve_got_all_permissions);
        Toast.makeText(getBaseContext(), R.string.weve_got_all_permissions, Toast.LENGTH_LONG).show();
    }


    @Override protected void onPostResume() {

        Log.v(TAG, "17");

        super.onPostResume();

        if (sentToSettings) {

            Log.v(TAG, "18");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "19");

                //Got Permission
                proceedAfterPermission();
            }
        }
    }
}
