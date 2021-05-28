# pacmc

**`pacmc`** is a package manager for Fabric Minecraft mods.

The aim of this project is to massively reduce the effort you have to put in for installing - and most importantly -
keeping your mods up to date.

## State

`pacmc` is currently in development. It will be released soon.

## Usage

The main command is `pacmc`. You can add `-h` to any command to get help.

### Archives

Archives are the places (folders) where your mods are stored. Your `.minecraft` folder is an archive by default, but you
can add more (for example to manage mods on a server, which `pacmc` is designed for aswell).

#### Add an archive

```zsh
pacmc archive add myarchive ./path/to/my/archive
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

_this searches for mods for the latest version by default_

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
pacmc install Fabric API
```

or to a specific archive:

```zsh
pacmc install -a myarchive 447425
```

_when installing using a search term, you may be prompted to select a mod from a couple of options_
