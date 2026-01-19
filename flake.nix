{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = {
    self,
    flake-parts,
    ...
  } @ inputs:
    flake-parts.lib.mkFlake {inherit inputs;} {
      systems = ["x86_64-linux"];

      perSystem = {
        config,
        lib,
        pkgs,
        system,
        ...
      }: let
        jdk = pkgs.javaPackages.compiler.temurin-bin.jdk-25;

        gradle = pkgs.gradle.override {
            javaToolchains = [
                jdk
                pkgs.temurin-bin
            ];
            java = jdk;
        };
       in {
         devShells.default = pkgs.mkShell {
           name = "Fluava";
           packages = with pkgs; [git jdk gradle maven];
           JDK24 = jdk;
         };
       };
    };
}