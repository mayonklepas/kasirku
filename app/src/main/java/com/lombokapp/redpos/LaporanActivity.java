package com.lombokapp.redpos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class LaporanActivity extends AppCompatActivity {

    ListView lvdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);
        lvdata=findViewById(R.id.lvdata);
        loaddatalist();
    }

    private void loaddatalist(){
        List<Listviewglobaladapter.listglobalmodel> ls=new ArrayList<>();
        ls.add(new Listviewglobaladapter.listglobalmodel("0","Laporan Penjualan","Lihat hasil penjualan anda, tentukan sendiri periode penjualan yang ingin anda lihat"));
        ls.add(new Listviewglobaladapter.listglobalmodel("1","Laporan Pembelian","Lihat hasil pembelian anda, tentukan sendiri periode pembelian yang ingin anda lihat"));
        ls.add(new Listviewglobaladapter.listglobalmodel("2","Laporan Stok Barang","Lihat stok terakhir persediaan anda"));
        ls.add(new Listviewglobaladapter.listglobalmodel("3","Ranking Penjualan Barang","Lihat ranking penjualan barang anda, untuk mengetahui barang terlaris"));
        ArrayAdapter<String> adapter=new Listviewglobaladapter(this,ls);
        lvdata.setAdapter(adapter);

        lvdata.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Intent in=new Intent(LaporanActivity.this,LihatLaporanActivity.class);
                    in.putExtra("tipe","penjualan");
                    startActivity(in);
                }else if(position==1){
                    Intent in=new Intent(LaporanActivity.this,LihatLaporanActivity.class);
                    in.putExtra("tipe","pembelian");
                    startActivity(in);
                }else if(position==2){
                    Intent in=new Intent(LaporanActivity.this,LihatLaporanActivity.class);
                    in.putExtra("tipe","stok");
                    startActivity(in);
                }else if(position==3){
                    Intent in=new Intent(LaporanActivity.this,LihatLaporanActivity.class);
                    in.putExtra("tipe","ranking");
                    startActivity(in);
                }
            }
        });
    }


}
