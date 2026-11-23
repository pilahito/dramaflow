package com.pilahito.dramaflow;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.nav);
        nav.getMenu().add(0, 1, 0, "Inicio").setIcon(android.R.drawable.ic_menu_gallery);
        nav.getMenu().add(0, 2, 1, "Shorts").setIcon(android.R.drawable.ic_media_play);
        nav.getMenu().add(0, 3, 2, "Ranking").setIcon(android.R.drawable.ic_menu_sort_by_size);
        nav.getMenu().add(0, 4, 3, "Perfil").setIcon(android.R.drawable.ic_menu_myplaces);

        if (savedInstanceState == null) mostrar(new CatalogoFragment());
        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case 1: mostrar(new CatalogoFragment()); return true;
                case 2: mostrar(new ShortsFragment()); return true;
                case 3: mostrar(new RankingFragment()); return true;
                default: mostrar(new PerfilFragment()); return true;
            }
        });
    }

    private void mostrar(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, f).commit();
    }

    // ── Datos legales: películas de dominio público / Creative Commons ──────
    public static class Serie {
        public final String titulo, genero, url, color;
        Serie(String t, String g, String u, String c) { titulo = t; genero = g; url = u; color = c; }
    }

    public static List<Serie> catalogo() {
        List<Serie> l = new ArrayList<>();
        l.add(new Serie("Big Buck Bunny", "Comedia · animación", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "#4C1D95"));
        l.add(new Serie("Sintel", "Fantasía · drama", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", "#7C2D12"));
        l.add(new Serie("Elephants Dream", "Ciencia ficción", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", "#1E3A8A"));
        l.add(new Serie("Tears of Steel", "Ciencia ficción", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", "#065F46"));
        l.add(new Serie("For Bigger Blazes", "Acción", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", "#7F1D1D"));
        l.add(new Serie("For Bigger Escapes", "Aventura", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", "#78350F"));
        l.add(new Serie("Subaru Outback", "Documental", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4", "#0C4A6E"));
        l.add(new Serie("Volkswagen GTI Review", "Motor", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4", "#3F3F46"));
        return l;
    }

    // ── Catálogo ────────────────────────────────────────────────────────────
    public static class CatalogoFragment extends Fragment {
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_catalogo, cont, false);
            RecyclerView lista = v.findViewById(R.id.lista);
            lista.setLayoutManager(new GridLayoutManager(getContext(), 2));
            lista.setAdapter(new Adaptador(catalogo()));
            return v;
        }
    }

    static class Adaptador extends RecyclerView.Adapter<Adaptador.VH> {
        private final List<Serie> datos;
        Adaptador(List<Serie> d) { datos = d; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_serie, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int i) {
            Serie s = datos.get(i);
            h.color.setBackground(new ColorDrawable(Color.parseColor(s.color)));
            h.titulo.setText(s.titulo);
            h.genero.setText(s.genero);
            h.itemView.setOnClickListener(v -> Reproducir.abrir(getActivityOrNull(v), s));
        }
        private android.app.Activity getActivityOrNull(View v) {
            return (android.app.Activity) v.getContext();
        }
        @Override public int getItemCount() { return datos.size(); }
        static class VH extends RecyclerView.ViewHolder {
            View color; TextView titulo, genero;
            VH(View v) { super(v); color = v.findViewById(R.id.color); titulo = v.findViewById(R.id.titulo); genero = v.findViewById(R.id.genero); }
        }
    }

    // ── Shorts: reproductor vertical ────────────────────────────────────────
    public static class ShortsFragment extends Fragment {
        private ExoPlayer player;
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_shorts, cont, false);
            ViewPager2 pager = v.findViewById(R.id.pager);
            pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
            pager.setAdapter(new FragmentStateAdapter(this) {
                @Override public int getItemCount() { return catalogo().size(); }
                @NonNull @Override public Fragment createFragment(int pos) { return ShortFragment.nuevo(catalogo().get(pos)); }
            });
            return v;
        }
    }

    public static class ShortFragment extends Fragment {
        static ShortFragment nuevo(Serie s) {
            ShortFragment f = new ShortFragment();
            Bundle b = new Bundle();
            b.putString("titulo", s.titulo); b.putString("url", s.url);
            f.setArguments(b); return f;
        }
        private ExoPlayer player;
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.item_short, cont, false);
            Bundle a = getArguments();
            ((TextView) v.findViewById(R.id.titulo)).setText(a.getString("titulo"));
            PlayerView pv = v.findViewById(R.id.player);
            player = new ExoPlayer.Builder(requireContext()).build();
            pv.setPlayer(player);
            player.setMediaItem(MediaItem.fromUri(a.getString("url")));
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.prepare(); player.setPlayWhenReady(true);
            return v;
        }
        @Override public void onPause() { super.onPause(); if (player != null) player.pause(); }
        @Override public void onResume() { super.onResume(); if (player != null) player.play(); }
        @Override public void onDestroyView() { super.onDestroyView(); if (player != null) { player.release(); player = null; } }
    }

    // ── Ranking ─────────────────────────────────────────────────────────────
    public static class RankingFragment extends Fragment {
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_ranking, cont, false);
            List<Serie> l = catalogo();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < l.size(); i++) sb.append(i + 1).append(".  ").append(l.get(i).titulo).append("\n     ").append(l.get(i).genero).append("\n\n");
            ((TextView) v.findViewById(R.id.lista_ranking)).setText(sb.toString());
            return v;
        }
    }

    // ── Perfil ──────────────────────────────────────────────────────────────
    public static class PerfilFragment extends Fragment {
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            return inf.inflate(R.layout.fragment_perfil, cont, false);
        }
    }

    // ── Reproductor a pantalla completa (reutiliza el pager en modo película) ─
    static class Reproducir {
        static void abrir(android.app.Activity act, Serie s) {
            if (act == null) return;
            androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(act);
            PlayerView pv = new PlayerView(act);
            ExoPlayer player = new ExoPlayer.Builder(act).build();
            pv.setPlayer(player);
            player.setMediaItem(MediaItem.fromUri(s.url));
            player.prepare(); player.setPlayWhenReady(true);
            b.setView(pv);
            androidx.appcompat.app.AlertDialog d = b.create();
            d.setOnDismissListener(x -> player.release());
            d.show();
        }
    }
}
