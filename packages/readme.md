## Build instructions

### rpm

```sh
# to get the sources
spectool -g pacmc.spec
# to run a mock build
fedpkg --release f36 mockbuild
```

See also:

- https://docs.fedoraproject.org/en-US/quick-docs/creating-rpm-packages/
- https://rpm-packaging-guide.github.io/

### Arch Linux

```sh
# to build the package
makepkg --printsrcinfo > .SRCINFO
# commit the relevant files
git add PKGBUILD .SRCINFO
```

See also:

- https://wiki.archlinux.org/title/AUR_submission_guidelines
- https://wiki.archlinux.org/title/Arch_package_guidelines

### Homebrew

```sh
# to run a test install
brew install --build-from-source --verbose --debug pacmc
# to audit the formula (audits all)
brew audit --strict --online
# to run the test
brew test pacmc
```

See also:

- https://docs.brew.sh/Formula-Cookbook

### nixpkg

// TODO

### Windows (scoop)

The scoop package does not have to be built - it is just a json file with information about the binary, which is stored somewhere else.
