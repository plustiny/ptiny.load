#!/bin/bash
SRC=$1
dirs=(mdpi hdpi xhdpi xxhdpi xxxhdpi)
rate=(5% 7.5% 10% 15% 20%)

magick $SRC -resize "760x760" -filter box -background none -gravity center -extent "960x960" PNG8:ic_launcher.png
magick $SRC -resize "960x960" -filter box -background "%[pixel:p{480,480}]" -alpha remove -alpha off PNG8:ic_launcher_round.png
magick ic_launcher_round.png -alpha set -background none \
  \( -size 960x960 xc:none -fill white -draw "circle 480,480 480,40" \) \
  -compose CopyOpacity -composite PNG8:ic_launcher_round.png 

for i in "${!dirs[@]}"; do
  r="${rate[i]}"
  d="mipmap-${dirs[i]}"
  mkdir -p "$d"
  magick ic_launcher.png -resize "$r" -filter box PNG8:"$d/ic_launcher.png"
  magick ic_launcher_round.png -resize "$r" -filter box PNG8:"$d/ic_launcher_round.png"
done

