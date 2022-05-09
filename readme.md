# pacmc

**`pacmc`** is a package manager (and soon to be launcher) for Minecraft mods.

The aim of this project is to massively reduce the effort you have to put in to installing - and most importantly -
keeping your mods up to date. Install Fabric, Quilt and Forge mods from Modrinth and Curseforge
(temporarily disabled)!

To install pacmc, visit the [**Installation** section](#installation).

The current version of pacmc is already pretty useful and works, however keep an eye on this project, as it will receive
major new features and improvements in the near future!

You can chat and ask questions on [**Matrix**](https://matrix.to/#/#kotlinmc:axay.net) (or
[Discord](https://discord.com/invite/CJDUVuJ)).

> Support for the new Curseforge API is in development!
>
> **Contributions for the following things will be very welcome**:
> - an icon for pacmc
> - an OpenGL 3D skin renderer

## Usage

The main command is `pacmc`. You can add `-h` to any command to get help.

### Command overview

```
Commands:
  search   Search for mods
  archive  Manage archives
  install  Install content to an archive
  remove   Remove content from an archive
  update   Update content installed to an archive
  list     List content installed to an archive
  refresh  Refresh an archive and all content installed to it
```

### Quick start

After you have [installed](#installation) pacmc, you start as follows:

```bash
# add the .minecraft folder as an archive
pacmc archive init
# search for mods
pacmc search sodium
# install a mod
pacmc install lithium
```

### Archives

Archives are the places (folders) where your mods are stored. Your `.minecraft` folder is an archive by default, but you
can add more (for example to manage mods on a server, which `pacmc` is designed for as well).

#### Init the default archive

To add the `.minecraft` folder as an archive:

```zsh
pacmc archive init
```

#### Add an archive

```zsh
pacmc archive create myarchive ./path/to/my/archive
```

#### List all existing archives

```zsh
pacmc archive list
```

#### Remove an archive

```zsh
pacmc archive remove myarchive
```

### Search for mods

```zsh
pacmc search sodium
# or
pacmc search "Fabric API"
```

### Install a mod

```zsh
pacmc install iris
```

or to a specific archive:

```zsh
pacmc install 447425 -a myarchive
```

## Installation

| Platform                          | Instructions                                                                                       | Location                                                                                       | Package Manager                                                       |
|-----------------------------------|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| **Arch Linux** and **Manjaro**    | install using [an AUR helper](https://wiki.archlinux.org/title/AUR_helpers) <br> e.g. `paru pacmc` | `pacmc` [in AUR](https://aur.archlinux.org/packages/pacmc/)                                    | any AUR helper                                                        |
| **macOS** or any **Linux** distro | <del> `brew install pacmc` </del>                                                                  | planned for the next release                                                                   | [Homebrew](https://brew.sh/)                                          |
| any **Linux** distro              | <del>`flatpak install net.axay.pacmc` </del>                                                       | coming soon, definitely with gui                                                               | [Flatpak](https://flatpak.org/)                                       |
| **NixOS** or any **Linux** distro | coming soon                                                                                        | coming soon                                                                                    | [NixOS](https://nixos.org/)                                           |
| **Windows**                       | (`scoop bucket add games`) <br> `scoop install pacmc`                                              | `pacmc` [in scoop-games](https://github.com/Calinou/scoop-games/blob/master/bucket/pacmc.json) | [scoop](https://scoop.sh)                                             |
| **Windows** (gui only)            | <del> Install pacmc from the Microsoft Store </del>                                                | considered for gui                                                                             | [Microsoft Store](https://www.microsoft.com/de-de/store/apps/windows) |
| **Windows**                       | <del> `winget install pacmc` </del>                                                                | postponed, only supports legacy installers                                                     | [winget](https://github.com/microsoft/winget-cli)                     |

#### Other

If the installation methods above don't fit your needs, you can also install pacmc manually. Keep in mind though, that
this way pacmc won't receive updates automatically.

Download one of the released archives from the [releases page](https://github.com/bluefireoly/pacmc/releases). Extract
the archive. Inside there will be a `bin` directory. Run the shell script using `./pacmc`.
