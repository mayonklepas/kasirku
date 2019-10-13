package com.lombokapp.redpos;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TambahPembelianActivity extends AppCompatActivity {

    FloatingActionButton fbadd;
    ImageView breload;
    Button bsimpan;
    RecyclerView rvdata;
    RecyclerView.LayoutManager layman;
    TambahPembelianAdapter adapter;
    Dblocalhelper dbo;
    public static ArrayList<TambahPembelianModel> lsdata = new ArrayList<>();
    EditText ednotrans, edtanggaltrans, eddesk, edkodebarang, ednofaktur;
    ImageView bimg_barcode,bimg_tanggal;
    TextView ltotal;
    NumberFormat nf = NumberFormat.getInstance();
    String kode_transaksi="";
    Calendar cal=Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pembelian);
        fbadd = findViewById(R.id.fbadd);
        breload = findViewById(R.id.breload);
        rvdata = findViewById(R.id.rvdata);
        ednotrans = findViewById(R.id.ednotrans);
        ednofaktur = findViewById(R.id.ednofaktur);
        edtanggaltrans = findViewById(R.id.edtanggaltrans);
        eddesk = findViewById(R.id.eddesk);
        edkodebarang = findViewById(R.id.edkodebarang);
        bimg_barcode = findViewById(R.id.bimg_barcode);
        bimg_tanggal = findViewById(R.id.bimg_tanggal);
        ltotal = findViewById(R.id.ltotal);
        bsimpan = findViewById(R.id.bsimpan);
        layman = new LinearLayoutManager(this);
        rvdata.setLayoutManager(layman);
        rvdata.setHasFixedSize(true);
        rvdata.setItemAnimator(new DefaultItemAnimator());
        adapter = new TambahPembelianAdapter(lsdata, this);
        rvdata.setAdapter(adapter);
        ednotrans.setEnabled(false);
        edtanggaltrans.setEnabled(false);
        dbo = new Dblocalhelper(this);
        adddata();
        lsdata.clear();
        Bundle ex = getIntent().getExtras();
        if (ex != null) {
            ednotrans.setText(ex.getString("kode_pembelian_master"));
            kode_transaksi=ex.getString("kode_pembelian_master");
            loaddata(ex.getString("kode_pembelian_master"));
            SQLiteDatabase db = dbo.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT tanggal_pembelian,no_faktur,deskripsi " +
                    "FROM pembelian_master WHERE kode_pembelian_master='" + ex.getString("kode_pembelian_master") + "' LIMIT 1", null);
            if (c.moveToFirst()) {
                edtanggaltrans.setText(c.getString(0));
                ednofaktur.setText(c.getString(1));
                eddesk.setText(c.getString(2));
            }
            c.close();
            db.close();
        } else {
            ednotrans.setText(dbo.getkodetransaksi("PB"));
            edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            kode_transaksi="";
        }
        savedata();
        caribarang();
        caribarcode();
        gettanggal();

    }

    private void caribarcode() {
        bimg_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(TambahPembelianActivity.this, BarcodeActivity.class);
                startActivityForResult(in, 1);
            }
        });
    }

    private void caribarang(){
        fbadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(TambahPembelianActivity.this,CariBarangActivity.class);
                in.putExtra("tipe_transaksi","beli");
                startActivity(in);
            }
        });
    }

    private void adddata() {
        edkodebarang.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String kode = v.getText().toString();
                    rawadddata(kode);

                }
                return false;
            }
        });
    }

    private void loaddata(final String kode_pembelian_master) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbo.getReadableDatabase();
                try {

                    String query = "SELECT pd.kode_barang,nama_barang,satuan_barang,pd.harga_beli," +
                            "jumlah,pd.harga_beli*jumlah AS total,gambar_barang " +
                            "FROM pembelian_detail pd INNER JOIN persediaan ps ON " +
                            "pd.kode_barang=ps.kode_barang WHERE kode_pembelian_master='" + kode_pembelian_master + "'";
                    Cursor c = db.rawQuery(query, null);
                    while (c.moveToNext()) {

                        lsdata.add(new TambahPembelianModel(c.getString(0), c.getString(1),
                                c.getString(2), c.getDouble(3), c.getDouble(4),
                                c.getDouble(5), c.getString(6)));
                    }
                    c.close();

                    adapter.notifyDataSetChanged();
                    double total = 0;
                    for (int i = 0; i < lsdata.size(); i++) {
                        total = total + lsdata.get(i).getTotal();
                    }
                    ltotal.setText(nf.format(total));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                }
            }
        }, 100);


    }

    private void rawadddata(final String kode_barang){
        SQLiteDatabase db = dbo.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT kode_barang,nama_barang,satuan_barang,harga_beli,gambar_barang " +
                "FROM persediaan WHERE kode_barang='" + kode_barang + "' LIMIT 1", null);
        if (c.moveToFirst()) {
            if (c.getString(0).equals("") || c.getString(0) == null) {
                Toast.makeText(TambahPembelianActivity.this, "Barang Tidak Ditemukan", Toast.LENGTH_SHORT).show();
            } else {

                int posisiindex=-1;
                for (int i = 0; i < lsdata.size(); i++) {
                    TambahPembelianModel inmodel= lsdata.get(i);
                    if(inmodel.getKode_barang().equals(kode_barang)){
                        posisiindex=i;
                    }
                }
                if(posisiindex<0) {
                    lsdata.add(new TambahPembelianModel(c.getString(0), c.getString(1),
                            c.getString(2), c.getDouble(3), 1.0,
                            (c.getDouble(3) * 1), c.getString(4)));
                }else{
                    double jumlahawal=lsdata.get(posisiindex).getJumlah();
                    double harga_beli=lsdata.get(posisiindex).getHarga_beli();
                    lsdata.get(posisiindex).setJumlah(jumlahawal+1);
                    lsdata.get(posisiindex).setTotal(harga_beli*(jumlahawal+1));
                }
                adapter.notifyDataSetChanged();
                double total = 0;
                for (int i = 0; i < lsdata.size(); i++) {
                    total = total + lsdata.get(i).getTotal();
                }
                ltotal.setText(nf.format(total));
            }

        } else {
            Toast.makeText(TambahPembelianActivity.this, "Barang Tidak Ditemukan", Toast.LENGTH_SHORT).show();
        }
        c.close();
        db.close();

    }

    private void savedata() {
        bsimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lsdata.size()<=0){
                    Toast.makeText(TambahPembelianActivity.this, "Proses Ditolak, Anda Belum Memasukan data", Toast.LENGTH_SHORT).show();
                }else {
                SQLiteDatabase db = dbo.getWritableDatabase();
                String currenttime = new SimpleDateFormat("yyyyMMddHHmmsszzz").format(new Date());
                try {
                    db.beginTransaction();
                    if (kode_transaksi.equals("") || kode_transaksi == null) {
                        db.execSQL("INSERT INTO pembelian_master(kode_pembelian_master,no_faktur,tanggal_pembelian,deskripsi,last_update,date_created) " +
                                "VALUES('" + ednotrans.getText().toString() + "','" + ednofaktur.getText().toString() + "'," +
                                "'" + edtanggaltrans.getText().toString() + "','" + eddesk.getText().toString() + "'," +
                                "'" + currenttime + "'," +
                                "'" + currenttime + "')");
                        for (int i = 0; i < lsdata.size(); i++) {
                            db.execSQL("INSERT INTO pembelian_detail(kode_pembelian_detail,kode_pembelian_master," +
                                    "kode_barang,jumlah,harga_beli) " +
                                    "VALUES('" + ednotrans.getText().toString() + i + "','" + ednotrans.getText().toString() + "'," +
                                    "'" + lsdata.get(i).getKode_barang() + "'," + Oneforallfunc.validdouble(lsdata.get(i).getJumlah()) + "," + Oneforallfunc.validdouble(lsdata.get(i).getHarga_beli()) + ")");
                            db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + lsdata.get(i).getJumlah() + " " +
                                    "WHERE kode_barang='" + lsdata.get(i).getKode_barang() + "' ");
                        }
                    } else {

                        db.execSQL("UPDATE pembelian_master  SET kode_pembelian_master='" + ednotrans.getText().toString() + "'," +
                                "no_faktur='" + ednofaktur.getText().toString() + "',tanggal_pembelian='" + edtanggaltrans.getText().toString() + "'," +
                                "deskripsi='" + eddesk.getText().toString() + "',last_update='" + currenttime + "' " +
                                "WHERE kode_pembelian_master='" + kode_transaksi + "'");
                        Cursor c = db.rawQuery("SELECT kode_pembelian_detail,kode_barang,jumlah " +
                                "FROM pembelian_detail WHERE kode_pembelian_master='" + kode_transaksi + "'", null);
                        while (c.moveToNext()) {
                            db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang-" + c.getDouble(2) + " " +
                                    "WHERE kode_barang='" + c.getString(1) + "' ");
                            //db.execSQL("DELETE FROM pembelian_detail WHERE kode_pembelian_detail='"+c.getString(0)+"'");
                        }

                        db.execSQL("DELETE FROM pembelian_detail WHERE kode_pembelian_master='" + kode_transaksi + "'");

                        for (int i = 0; i < lsdata.size(); i++) {
                            String nodetail = ednotrans.getText().toString() + "/" + i;
                            db.execSQL("INSERT INTO pembelian_detail(kode_pembelian_detail,kode_pembelian_master," +
                                    "kode_barang,jumlah,harga_beli) " +
                                    "VALUES('" + nodetail + "','" + ednotrans.getText().toString() + "'," +
                                    "'" + lsdata.get(i).getKode_barang() + "'," + Oneforallfunc.validdouble(lsdata.get(i).getJumlah()) + "," +
                                    Oneforallfunc.validdouble(lsdata.get(i).getHarga_beli()) + ")");
                            db.execSQL("UPDATE persediaan SET jumlah_barang=jumlah_barang+" + lsdata.get(i).getJumlah() + " " +
                                    "WHERE kode_barang='" + lsdata.get(i).getKode_barang() + "' ");
                        }

                    }
                    db.setTransactionSuccessful();
                    Toast.makeText(TambahPembelianActivity.this, "Data Pembelian Berhasil Disimpan", Toast.LENGTH_SHORT).show();
                    lsdata.clear();
                    adapter.notifyDataSetChanged();
                    ednotrans.setText(dbo.getkodetransaksi("PB"));
                    edtanggaltrans.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    eddesk.setText("");
                    edkodebarang.setText("");
                    ednofaktur.setText("");
                    ltotal.setText("0");
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
            }
        });
    }

    private void gettanggal(){
        bimg_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd=new DatePickerDialog(TambahPembelianActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        cal.set(Calendar.YEAR,year);
                        cal.set(Calendar.MONTH,month);
                        cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        String tanggalkini=new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                        edtanggaltrans.setText(tanggalkini);
                    }
                },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));

                dpd.show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
        double total = 0;
        for (int i = 0; i < lsdata.size(); i++) {
            total = total + lsdata.get(i).getTotal();
        }
        ltotal.setText(nf.format(total));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                String kode=data.getData().toString();
                edkodebarang.setText(kode);
                rawadddata(kode);
            }
        }
    }

    public class TambahPembelianAdapter extends RecyclerView.Adapter {
        ArrayList<TambahPembelianModel> model = new ArrayList<>();
        Context ct;
        NumberFormat nf = NumberFormat.getInstance();
        int content = 0;

        public TambahPembelianAdapter(ArrayList<TambahPembelianModel> model, Context ct) {
            this.model = model;
            this.ct = ct;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater lin = LayoutInflater.from(parent.getContext());
            View v = lin.inflate(R.layout.adapter_tambah_pembelian, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof Holder) {
                final Holder h = (Holder) holder;
                h.lnama_barang.setText(model.get(position).getNama_barang());
                h.lkode_barang.setText(model.get(position).getKode_barang());
                h.lharga_jual.setText("Harga : " + nf.format(model.get(position).getHarga_beli()));
                h.edjumlah.setText(nf.format(model.get(position).getJumlah()));
                double total_harga = model.get(position).getHarga_beli() * model.get(position).getJumlah();
                h.ltotal_harga.setText(nf.format(total_harga));
                Glide.with(ct).
                        load(new File(model.get(position).getGambar_barang())).
                        placeholder(R.drawable.ic_assessment_70dp).
                        centerCrop().
                        diskCacheStrategy(DiskCacheStrategy.ALL).
                        into(h.gambar_barang);

                h.edjumlah.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId==EditorInfo.IME_ACTION_DONE){
                            model.get(position).setJumlah(Oneforallfunc.Stringtodouble(v.getText().toString()));
                            double total_harga = model.get(position).getJumlah() * model.get(position).getHarga_beli();
                            model.get(position).setTotal(total_harga);
                            notifyItemChanged(position);
                            double total = 0;
                            for (int i = 0; i < lsdata.size(); i++) {
                                total = total + lsdata.get(i).getTotal();
                            }
                            ltotal.setText(nf.format(total));
                        }
                        return false;
                    }
                });

                h.bset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        model.get(position).setJumlah(Oneforallfunc.Stringtodouble(h.edjumlah.getText().toString()));
                        double total_harga = model.get(position).getJumlah() * model.get(position).getHarga_beli();
                        model.get(position).setTotal(total_harga);
                        notifyItemChanged(position);
                        double total = 0;
                        for (int i = 0; i < lsdata.size(); i++) {
                            total = total + lsdata.get(i).getTotal();
                        }
                        ltotal.setText(nf.format(total));
                    }
                });

                h.img_hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(ct);
                        adb.setTitle("Konfirmasi");
                        adb.setMessage("Yakin ingin menghapus " + model.get(position).getNama_barang() + " ? ");
                        adb.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    model.remove(position);
                                    notifyDataSetChanged();
                                    double total = 0;
                                    for (int i = 0; i < lsdata.size(); i++) {
                                        total = total + lsdata.get(i).getTotal();
                                    }
                                    ltotal.setText(nf.format(total));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                        adb.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        adb.show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return model.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView lnama_barang,lkode_barang, lharga_jual, ltotal_harga;
        EditText edjumlah;
        Button bset;
        ImageView gambar_barang, img_hapus;

        public Holder(View itemView) {
            super(itemView);
            lnama_barang = itemView.findViewById(R.id.lnama_barang);
            lkode_barang=itemView.findViewById(R.id.lkode_barang);
            lharga_jual = itemView.findViewById(R.id.ljudul);
            ltotal_harga = itemView.findViewById(R.id.ltotal_harga_final);
            edjumlah = itemView.findViewById(R.id.edjumlah);
            bset = itemView.findViewById(R.id.bset);
            gambar_barang = itemView.findViewById(R.id.gambar_barang);
            img_hapus = itemView.findViewById(R.id.img_hapus);
        }
    }

    public static class TambahPembelianModel {
        String kode_barang, nama_barang, satuan;
        double harga_beli, jumlah, total;
        String gambar_barang;

        public TambahPembelianModel(String kode_barang, String nama_barang, String satuan, double harga_beli, double jumlah, double total, String gambar_barang) {
            this.kode_barang = kode_barang;
            this.nama_barang = nama_barang;
            this.satuan = satuan;
            this.harga_beli = harga_beli;
            this.jumlah = jumlah;
            this.total = total;
            this.gambar_barang = gambar_barang;
        }

        public String getKode_barang() {
            return kode_barang;
        }

        public void setKode_barang(String kode_barang) {
            this.kode_barang = kode_barang;
        }

        public String getNama_barang() {
            return nama_barang;
        }

        public void setNama_barang(String nama_barang) {
            this.nama_barang = nama_barang;
        }

        public String getSatuan() {
            return satuan;
        }

        public void setSatuan(String satuan) {
            this.satuan = satuan;
        }

        public double getHarga_beli() {
            return harga_beli;
        }

        public void setHarga_beli(double harga_beli) {
            this.harga_beli = harga_beli;
        }

        public double getJumlah() {
            return jumlah;
        }

        public void setJumlah(double jumlah) {
            this.jumlah = jumlah;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public String getGambar_barang() {
            return gambar_barang;
        }

        public void setGambar_barang(String gambar_barang) {
            this.gambar_barang = gambar_barang;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof TambahPembelianModel){
                return (((TambahPembelianModel) obj).getKode_barang().equals(this.getKode_barang()));
            }
            return false;
        }
    }
}
