# pacmc

**`pacmc`** is a package manager for Fabric Minecraft mods.

The aim of this project is to massively reduce the effort you have to put in to installing - and most importantly -
keeping your mods up to date.

The current version of pacmc is already pretty useful and works, however keep an eye on this project, as it will receive
major new features and improvements in the near future!

To install pacmc, visit the [**Installation** section](#installation).

Chat and ask questions on [**Matrix**](https://matrix.to/#/#kotlinmc:axay.net) (or
[Discord](https://discord.com/invite/CJDUVuJ))

> I am currently rewriting pacmc to use the Modrinth v2 API as well as the new Curseforge API.
> Until then, you might experience issues while pacmc is still using the old APIs of these
> two platforms.
> 
> The rewrite will also introduce some major improvements and new features, so stay tuned!
>
> **Contributions for the following things will be very welcome**:
> - an icon for pacmc
> - an OpenGL 3D skin renderer

## Usage

The main command is `pacmc`. You can add `-h` to any command to get help.

### Command overview

```
Commands:
  install  Installs a minecraft mod
  update   Updates the mods inside an archive
  search   Searches for mods
  list     Lists the installed mods
  remove   Removes a minecraft mod
  archive  Manages your mod archives
  init     Sets the pacmc defaults
  refresh  Refreshes the local mod files according to the database
  load     Loads one archive into another
  info     Displays the formatted project description
  debug    Prints debug information
```

### Quick start (for installing mods)

```bash
# add the .minecraft folder as an archive
pacmc init
# search for mods
pacmc search minihud
# install a mod
pacmc install lithium
```

### Archives

Archives are the places (folders) where your mods are stored. Your `.minecraft` folder is an archive by default, but you
can add more (for example to manage mods on a server, which `pacmc` is designed for aswell).

#### Init the default archive

To add the `.minecraft` folder as an archive:

```zsh
pacmc init
```

#### Add an archive

```zsh
pacmc archive add myarchive [./path/to/my/archive]
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

_this searches for mods for the latest minecraft version by default_

For a specific game version:

```zsh
pacmc search -g 1.15.2 "Fabric API"
```

or version independent:

```zsh
pacmc search -i "minimap"
```

### Install a mod

```zsh
# via the mod id
pacmc install 447425
# or via a search term
pacmc install tweakeroo
```

or to a specific archive:

```zsh
pacmc install -a myarchive 447425
```

_when installing using a search term, you may be prompted to select a mod from a couple of options_

## Installation

| Platform                       | Instructions                                                                                       | Location                                                                                       | Package Manager                                                       |
|--------------------------------|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| **Arch Linux** and **Manjaro** | install using [an AUR helper](https://wiki.archlinux.org/title/AUR_helpers) <br> e.g. `paru pacmc` | `pacmc` [in AUR](https://aur.archlinux.org/packages/pacmc/)                                    | any AUR helper                                                        |
| **macOS** and **Linux**        | <del> `brew install pacmc` </del>                                                                  | planned for the next release                                                                   | [Homebrew](https://brew.sh/)                                          |
| **Linux**                      | <del>`flatpak install net.axay.pacmc` </del>                                                       | coming soon, definitely with gui                                                               | [Flatpak](https://flatpak.org/)                                       |
| **Windows**                    | (`scoop bucket add games`) <br> `scoop install pacmc`                                              | `pacmc` [in scoop-games](https://github.com/Calinou/scoop-games/blob/master/bucket/pacmc.json) | [scoop](https://scoop.sh)                                             |
| **Windows**                    | <del> `winget install pacmc` </del>                                                                | postponed, only supports legacy installers                                                     | [winget](https://github.com/microsoft/winget-cli)                     |
| **Windows** (gui only)         | <del> Install pacmc from the Microsoft Store </del>                                                | considered for gui                                                                             | [Microsoft Store](https://www.microsoft.com/de-de/store/apps/windows) |

#### Other

If the installation methods above don't fit your needs, you can also install pacmc manually. Keep in mind though, that
this way pacmc won't receive updates automatically.

Download one of the released archives from the [releases page](https://github.com/bluefireoly/pacmc/releases). Extract
the archive. Inside there will be a `bin` directory. Run the shell script using `./pacmc`.
