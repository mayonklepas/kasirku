package com.lombokapp.kasiroff;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TambahPersediaanActivity extends AppCompatActivity {

    EditText edkode_barang;
    EditText ednama_barang;
    EditText edharga_beli;
    EditText edharga_jual;
    EditText eddiskon;
    EditText edjumlah;
    EditText edsatuan;
    Spinner stipe_persediaan;
    ImageView img_barang, bgetimage, bimg_barcode;
    Button bsimpan;
    Dblocalhelper dbo;
    String kode_barang = "";
    String[] tipe_persediaan_item = {"Barang Jadi", "Barang Racikan"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_persediaan);
        edkode_barang = findViewById(R.id.edkode_barang);
        ednama_barang = findViewById(R.id.ednama_barang);
        edharga_beli = findViewById(R.id.edharga_beli);
        edharga_jual = findViewById(R.id.edharga_jual);
        eddiskon = findViewById(R.id.eddiskon);
        edjumlah = findViewById(R.id.edjumlah);
        edsatuan = findViewById(R.id.edsatuan);
        img_barang = findViewById(R.id.img_barang);
        bgetimage = findViewById(R.id.bgetimage);
        bimg_barcode = findViewById(R.id.bimg_barcode);
        bsimpan = findViewById(R.id.bsimpan);
        stipe_persediaan = findViewById(R.id.stipe_persediaan);
        dbo = new Dblocalhelper(this);
        savedata();
        addimage();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            kode_barang = extras.getString("kode_barang");
            loaddata(kode_barang);
        }

        caribarcode();
        loadspinner();

    }


    private void loadspinner() {
        ArrayAdapter<String> spinadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, tipe_persediaan_item);
        stipe_persediaan.setAdapter(spinadapter);
        stipe_persediaan.setSelection(0);
        stipe_persediaan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    edjumlah.setEnabled(true);
                    edjumlah.setText("1");
                } else {
                    edjumlah.setEnabled(false);
                    edjumlah.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void caribarcode() {
        bimg_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(TambahPersediaanActivity.this, BarcodeActivity.class);
                startActivityForResult(in, 1);
            }
        });
    }

    private void savedata() {
        bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileimg = "";
                try {
                    BitmapDrawable bmpd = (BitmapDrawable) img_barang.getDrawable();
                    Bitmap bmp = bmpd.getBitmap();
                    File dirapp = new File(getFilesDir(), "kasirkuimage");
                    SQLiteDatabase dbr = dbo.getReadableDatabase();
                    Cursor c = dbr.rawQuery("SELECT COUNT(kode_barang),gambar_barang FROM persediaan WHERE kode_barang='" + kode_barang + "' LIMIT 1", null);
                    c.moveToFirst();
                    if (c.getInt(0)==0) {
                        fileimg = dirapp.getPath() + "/" + System.currentTimeMillis() + ".jpg";
                    } else {
                        File fldel = new File(getFilesDir(), "kasirkuimage/" + c.getString(1));
                        fldel.delete();
                        fileimg = dirapp.getPath() + "/" + System.currentTimeMillis() + ".jpg";
                    }


                    try {
                        FileOutputStream fos = new FileOutputStream(fileimg);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    fileimg = "none";
                    Toast.makeText(TambahPersediaanActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
                SQLiteDatabase db = dbo.getWritableDatabase();
                db.beginTransaction();
                String tipe_barang = String.valueOf(stipe_persediaan.getSelectedItemPosition());
                try {
                    String currenttime = new SimpleDateFormat("yyyyMMddHHmmsszzz").format(new Date());
                    if (kode_barang.equals("")) {
                        db.execSQL("INSERT INTO persediaan" +
                                "(kode_barang,nama_barang,satuan_barang,jumlah_barang,harga_beli,harga_jual,gambar_barang," +
                                "tipe_barang,diskon,date_created) " +
                                "VALUES('" + edkode_barang.getText().toString() + "'," +
                                "'" + ednama_barang.getText().toString() + "'," +
                                "'" + edsatuan.getText().toString() + "'," +
                                "" + Oneforallfunc.Stringtoint(edjumlah.getText().toString()) + "," +
                                "" + Oneforallfunc.Stringtodouble(edharga_beli.getText().toString()) + "," +
                                "" + Oneforallfunc.Stringtodouble(edharga_jual.getText().toString()) + "," +
                                "'" + fileimg + "'," +
                                "" + tipe_barang + "," +
                                ""+Oneforallfunc.Stringtodouble(eddiskon.getText().toString())+"," +
                                "'"+currenttime+"')");
                    } else {
                        db.execSQL("UPDATE persediaan SET kode_barang='" + edkode_barang.getText().toString() + "'," +
                                "nama_barang='" + ednama_barang.getText().toString() + "'," +
                                "satuan_barang='" + edsatuan.getText().toString() + "'," +
                                "jumlah_barang=" + Oneforallfunc.Stringtodouble(edjumlah.getText().toString()) + "," +
                                "harga_beli=" + Oneforallfunc.Stringtodouble(edharga_beli.getText().toString()) + "," +
                                "harga_jual=" + Oneforallfunc.Stringtodouble(edharga_jual.getText().toString()) + "," +
                                "gambar_barang='" + fileimg + "'," +
                                "tipe_barang=" + Oneforallfunc.Stringtoint(tipe_barang) + "," +
                                "diskon=" + Oneforallfunc.Stringtoint(eddiskon.getText().toString()) +" " +
                                "WHERE kode_barang='" + kode_barang + "' ");
                    }
                    db.setTransactionSuccessful();
                    Toast.makeText(TambahPersediaanActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TambahPersediaanActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction();
                    db.close();
                }

                if (tipe_barang.equals("1")) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(TambahPersediaanActivity.this);
                    adb.setTitle("Informasi");
                    adb.setMessage("Tambahkan Bahan Racikan?");
                    adb.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intn = new Intent(TambahPersediaanActivity.this, RacikActivity.class);
                            intn.putExtra("kode_barang", edkode_barang.getText().toString());
                            intn.putExtra("nama_barang", ednama_barang.getText().toString());
                            startActivity(intn);
                        }
                    });
                    adb.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    adb.setCancelable(false);
                    adb.show();
                }

            }
        });

    }

    private void addimage() {
        bgetimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(TambahPersediaanActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TambahPersediaanActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(TambahPersediaanActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TambahPersediaanActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }
                if (ActivityCompat.checkSelfPermission(TambahPersediaanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TambahPersediaanActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 1);
                    return;
                }
                AlertDialog.Builder adb = new AlertDialog.Builder(TambahPersediaanActivity.this);
                adb.setCancelable(false);
                adb.setTitle("Konfirmasi");
                adb.setMessage("Pilih Sumber Gambar");
                adb.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 3);
                    }
                });
                adb.setNeutralButton("Galeri", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
                    }
                });

                adb.setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                adb.show();

            }
        });
    }

    private void loaddata(final String kode_barang) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {
                    String query = "SELECT kode_barang,nama_barang,satuan_barang,harga_beli,harga_jual," +
                            "jumlah_barang,gambar_barang,tipe_barang,diskon FROM persediaan WHERE kode_barang='" + kode_barang + "' ";
                    Cursor c = db.rawQuery(query, null);
                    if (c.moveToFirst()) {
                        edkode_barang.setText(c.getString(0));
                        ednama_barang.setText(c.getString(1));
                        edsatuan.setText(c.getString(2));
                        edharga_beli.setText(c.getString(3));
                        edharga_jual.setText(c.getString(4));
                        edjumlah.setText(c.getString(5));
                        stipe_persediaan.setSelection(c.getInt(7));
                        eddiskon.setText(c.getString(8));
                        img_barang.setMaxWidth(200);
                        img_barang.getLayoutParams().width = 250;
                        img_barang.getLayoutParams().height = 250;
                        if (c.getString(6).equals("none")) {
                            img_barang.setImageResource(R.drawable.ic_image_black_24dp);
                        } else {
                            Bitmap bmp = BitmapFactory.decodeFile(c.getString(6));
                            img_barang.setImageBitmap(bmp);
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        }, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            try {
                img_barang.setMaxWidth(300);
                img_barang.getLayoutParams().width = 300;
                img_barang.getLayoutParams().height = 300;
                Uri uri = data.getData();
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap thumbbmp = ThumbnailUtils.extractThumbnail(bmp, 300, 300);
                img_barang.setImageBitmap(thumbbmp);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String kode = data.getData().toString();
                edkode_barang.setText(kode);
            }
        } else if (requestCode == 3 && resultCode==RESULT_OK) {
            img_barang.setMaxWidth(300);
            img_barang.getLayoutParams().width = 300;
            img_barang.getLayoutParams().height = 300;
            Bundle extra = data.getExtras();
            Bitmap imgbitmap = (Bitmap) extra.get("data");
            img_barang.setImageBitmap(imgbitmap);
        }

    }

}
