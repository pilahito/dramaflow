# DramaFlow

App Android de **series cortas** con catálogo de vídeos **libres** (dominio público y Creative Commons): catálogo en rejilla, reproductor vertical tipo *shorts*, ranking y perfil.

> No es un clon ni una modificación de ninguna app propietaria. Interfaz y código propios (MIT);
> el contenido de ejemplo es de Blender Foundation (CC BY) y del bucket de muestras de Google.

## Qué incluye

- **Inicio**: catálogo en rejilla con **portada real** (fotograma extraído de cada película),
  título, género y nota; **filtros por género** (Todos/Comedia/Fantasía/Sci-Fi).
- **Shorts**: pager vertical a pantalla completa con ExoPlayer (reproduce el vídeo al deslizar).
- **Ranking**: lista ordenada por nota (★).
- **Perfil**: info de la app y licencias del contenido.
- Reproductor a pantalla completa al tocar una serie.

## Compilar

Requiere JDK 17/21 y Android SDK 34:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Instalar

```bash
adb install -r DramaFlow-1.1-debug.apk
```

## Licencia

MIT (código). Los vídeos de ejemplo pertenecen a sus autores:
Blender Foundation (CC BY 3.0) y Google sample bucket.
