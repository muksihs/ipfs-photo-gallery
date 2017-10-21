#!/bin/bash

set -e
set -o pipefail
trap 'echo ERROR' ERR

IPFS="https://ipfs.macholibre.org/ipfs"
WAR="ipfs-photo-gallery"

cd "$(dirname "$0")"
gradle clean build
cd build/libs
#unpack the war
unzip "$WAR".war -d "$WAR"
#remove servlet stuff
rm -rf "$WAR"/META-INF
rm -rf "$WAR"/WEB-INF

TMP=ipfskey.txt
DIRKEY="QmUNLLsPACCz1vLxQVkXqqLX5R1X345qqfHbsf67hvA3Nn"

rawurlencode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""
  local pos c o

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * )               printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  echo "${encoded}"    # You can either set a return variable (FASTER) 
  REPLY="${encoded}"   #+or echo the result (EASIER)... or both... :p
}

find "$WAR" | while read x; do
	if [ ! -f "$x" ]; then continue; fi
	
	echo "Uploading $x"
	y=$(rawurlencode "$x")
	curl -# -D "$TMP" -X PUT --data-binary @"$x" $IPFS/"$DIRKEY"/"$y" | tee /dev/null
	DIRKEY="$(grep 'Ipfs-Hash:' "$TMP" | cut -f 2 -d ':' | sed 's/[[:space:]]*//g')"
	DIRKEY=$(rawurlencode "$DIRKEY")
	if [ "$DIRKEY"x = x ]; then
		echo "FATAL. LOST DIRKEY!"
		break
	fi
done

DIRKEY="$(grep 'Ipfs-Hash:' "$TMP" | cut -f 2 -d ':' | sed 's/[[:space:]]*//g')"
echo "FINAL DIR URL: $IPFS/$DIRKEY"

xdg-open "$IPFS/$DIRKEY"

echo "DONE."

