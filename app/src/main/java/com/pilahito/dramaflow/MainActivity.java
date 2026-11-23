package com.pilahito.dramaflow;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
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
        nav.getMenu().add(0, 3, 2, "TikTok").setIcon(android.R.drawable.ic_menu_camera);
        nav.getMenu().add(0, 4, 3, "Ranking").setIcon(android.R.drawable.ic_menu_sort_by_size);
        nav.getMenu().add(0, 5, 4, "Perfil").setIcon(android.R.drawable.ic_menu_myplaces);

        if (savedInstanceState == null) mostrar(new CatalogoFragment());
        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case 1: mostrar(new CatalogoFragment()); return true;
                case 2: mostrar(new ShortsFragment()); return true;
                case 3: mostrar(new TikTokFragment()); return true;
                case 4: mostrar(new RankingFragment()); return true;
                default: mostrar(new PerfilFragment()); return true;
            }
        });
    }

    private void mostrar(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, f).commit();
    }

    // ── Datos legales: películas de dominio público / Creative Commons ──────
    public static class Serie {
        public final String titulo, genero, url, categoria;
        public final int portada, nota;
        Serie(String t, String g, String u, String cat, int img, int n) {
            titulo = t; genero = g; url = u; categoria = cat; portada = img; nota = n;
        }
    }

    public static List<Serie> catalogo() {
        List<Serie> l = new ArrayList<>();
        l.add(new Serie("Big Buck Bunny", "Comedia · animación", "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4", "Comedia", R.drawable.bbb, 96));
        l.add(new Serie("Sintel", "Fantasía · drama", "https://archive.org/download/Sintel/sintel-2048-surround.mp4", "Fantasía", R.drawable.sintel, 94));
        l.add(new Serie("Elephants Dream", "Ciencia ficción", "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4", "Sci-Fi", R.drawable.ed, 88));
        l.add(new Serie("Tears of Steel", "Ciencia ficción", "https://download.blender.org/demo/movies/ToS/tears_of_steel_720p.mov", "Sci-Fi", R.drawable.tos, 91));
        l.add(new Serie("Sintel · tráiler", "Fantasía", "https://download.blender.org/durian/trailer/sintel_trailer-720p.mp4", "Fantasía", R.drawable.sintel_trailer, 90));
        l.add(new Serie("Sintel · avance", "Fantasía", "https://download.blender.org/durian/trailer/sintel_trailer-480p.mp4", "Fantasía", R.drawable.sintel_480, 87));
        return l;
    }

    // ── Catálogo ────────────────────────────────────────────────────────────
    public static class CatalogoFragment extends Fragment {
        private Adaptador adaptador;
        private String categoria = "Todos";

        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_catalogo, cont, false);
            RecyclerView lista = v.findViewById(R.id.lista);
            lista.setLayoutManager(new GridLayoutManager(getContext(), 2));
            adaptador = new Adaptador(catalogo());
            lista.setAdapter(adaptador);

            LinearLayout chips = v.findViewById(R.id.chips);
            for (String cat : new String[]{"Todos", "Comedia", "Fantasía", "Sci-Fi"}) {
                TextView chip = (TextView) inf.inflate(R.layout.chip_genero, chips, false);
                chip.setText(cat);
                chip.setOnClickListener(c -> { categoria = cat; adaptador.filtrar(cat); refrescarChips(chips, cat); });
                chips.addView(chip);
            }
            refrescarChips(chips, "Todos");
            return v;
        }

        private void refrescarChips(LinearLayout chips, String activa) {
            for (int i = 0; i < chips.getChildCount(); i++) {
                TextView c = (TextView) chips.getChildAt(i);
                boolean on = c.getText().toString().equals(activa);
                c.setTextColor(on ? 0xFFFFFFFF : 0xFFCFD8E6);
                c.setBackgroundResource(on ? R.drawable.fondo_chip_activo : R.drawable.fondo_chip);
            }
        }
    }

    static class Adaptador extends RecyclerView.Adapter<Adaptador.VH> {
        private final List<Serie> todos;
        private final List<Serie> datos = new ArrayList<>();
        Adaptador(List<Serie> d) { todos = d; datos.addAll(d); }

        void filtrar(String cat) {
            datos.clear();
            for (Serie s : todos) if (cat.equals("Todos") || s.categoria.equals(cat)) datos.add(s);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_serie, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int i) {
            Serie s = datos.get(i);
            h.portada.setImageResource(s.portada);
            h.nota.setText(String.valueOf(s.nota));
            h.titulo.setText(s.titulo);
            h.genero.setText(s.genero);
            h.itemView.setOnClickListener(v -> Reproducir.abrir((android.app.Activity) v.getContext(), s));
        }
        @Override public int getItemCount() { return datos.size(); }
        static class VH extends RecyclerView.ViewHolder {
            android.widget.ImageView portada; TextView titulo, genero, nota;
            VH(View v) {
                super(v);
                portada = v.findViewById(R.id.portada); titulo = v.findViewById(R.id.titulo);
                genero = v.findViewById(R.id.genero); nota = v.findViewById(R.id.nota);
            }
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
            pv.setUseController(false);
            pv.setKeepScreenOn(true);
            pv.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
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

    // ── TikTok: navegador dentro de la app (vista web oficial) ──────────────
    public static class TikTokFragment extends Fragment {
        private static final String URL = "https://www.tiktok.com/tag/shortdrama";
        private static final String UA =
            "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";
        private boolean aceptadas = false;
        private WebView web;
        private ProgressBar cargando;

        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_tiktok, cont, false);
            web = v.findViewById(R.id.web_tiktok);
            cargando = v.findViewById(R.id.cargando);

            WebSettings cfg = web.getSettings();
            cfg.setJavaScriptEnabled(true);
            cfg.setDomStorageEnabled(true);
            cfg.setDatabaseEnabled(true);
            cfg.setMediaPlaybackRequiresUserGesture(false);
            cfg.setLoadWithOverviewMode(true);
            cfg.setUseWideViewPort(true);
            cfg.setUserAgentString(UA);
            cfg.setJavaScriptCanOpenWindowsAutomatically(true);
            cfg.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            web.setBackgroundColor(0xFF000000);
            web.setWebViewClient(new WebViewClient() {
                @Override public boolean shouldOverrideUrlLoading(WebView w, android.webkit.WebResourceRequest r) {
                    String u = r.getUrl().toString();
                    return !(u.contains("tiktok.com"));
                }
                @Override public void onPageFinished(WebView w, String url) {
                    cargando.setVisibility(View.GONE);
                    aceptarCookies(w);
                    ((TextView) getView().findViewById(R.id.txt_url)).setText(url.replace("https://www.tiktok.com", "tiktok.com"));
                }
                @Override public void onPageStarted(WebView w, String url, android.graphics.Bitmap fav) {
                    cargando.setVisibility(View.VISIBLE);
                }
            });
            web.setWebChromeClient(new android.webkit.WebChromeClient());
            web.loadUrl(URL);

            v.findViewById(R.id.btn_atras).setOnClickListener(x -> { if (web.canGoBack()) web.goBack(); });
            v.findViewById(R.id.btn_adelante).setOnClickListener(x -> { if (web.canGoForward()) web.goForward(); });
            v.findViewById(R.id.btn_recargar).setOnClickListener(x -> web.reload());
            v.findViewById(R.id.btn_tiktok_app).setOnClickListener(x -> abrirExterno(URL));
            v.findViewById(R.id.btn_tiktok_navegador).setOnClickListener(x -> abrirExterno(URL));
            return v;
        }

        // Acepta el banner de cookies de TikTok para poder navegar sin estorbo.
        private void aceptarCookies(WebView w) {
            // Reintenta varias veces: el banner aparece con retraso y a veces en un iframe.
            for (int i = 0; i < 8; i++) {
                final int n = i;
                w.postDelayed(() -> {
                    if (web == null) return;
                    web.evaluateJavascript(
                        "(function(){try{" +
                        "const sel='button,a,div[role=button]';" +
                        "const b=[...document.querySelectorAll(sel)].find(e=>/^(permitir todas|allow all|aceptar todo|accept all)$/i.test((e.innerText||'').trim()));" +
                        "if(b){b.click();return 'clic';}" +
                        "return 'no';" +
                        "}catch(e){return 'err';}})();",
                        r -> { if (!aceptadas && r != null && r.contains("clic")) { aceptadas = true; } });
                }, 500L + n * 700L);
            }
        }

        private void abrirExterno(String url) {
            try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))); }
            catch (Exception e) { }
        }

        public boolean atras() {
            if (web != null && web.canGoBack()) { web.goBack(); return true; }
            return false;
        }

        @Override public void onDestroyView() {
            if (web != null) { web.destroy(); web = null; }
            super.onDestroyView();
        }
    }

    // ── Ranking ─────────────────────────────────────────────────────────────
    public static class RankingFragment extends Fragment {
        @Override public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b) {
            View v = inf.inflate(R.layout.fragment_ranking, cont, false);
            List<Serie> l = catalogo();
            StringBuilder sb = new StringBuilder();
            l.sort((x, y) -> Integer.compare(y.nota, x.nota));
            for (int i = 0; i < l.size(); i++)
                sb.append(i + 1).append(".   ").append(l.get(i).titulo)
                  .append("   ★ ").append(l.get(i).nota).append("\n      ")
                  .append(l.get(i).genero).append("\n\n");
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
