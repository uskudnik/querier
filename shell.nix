let channels = rec {
  pkgs = import <nixpkgs> {};
  # pkgs-master = import (fetchTarball https://github.com/NixOs/nixpkgs/archive/master.tar.gz) {};
};
in with channels;

pkgs.stdenv.mkDerivation rec {
  name = "env";
  buildInputs = [
    pkgs.stdenv
    pkgs.clojure
    pkgs.leiningen
  ];

  shellHook = ''
    SOURCE_DATE_EPOCH=$(date +%s)
    BASE_PATH=$PWD
  '';
}

