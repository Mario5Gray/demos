
#Lets use vim + Scala + ensime!

## First - configure gradle

For complete breakdown, see http://ensime.org/build_tools/gradle/

For Demonstration, re-use the provided init script:
``` 
$ { echo ; curl -s https://raw.githubusercontent.com/Mario5Gray/demos/master/configs/ensime.init.gradle; } >> ~/.gradle/init.gradle
```

## Installing Ensime plugins for vim:

See ensime.org/editors/vim/install for complete details:

### Ensure Vim is installed with the python2 package
With Ubuntu 16.04+
```
sudo apt-get install vim vim-nox-py2
```

#### Cygwin
Install packages `vim-common` and `vim`. 

### Installing `ensime-vim` dependencies
```
$ pip install websocket-client sexpdata
```

#### Obtain vim-plug or vundle (using vim-plug) for vim plugin mgmt.
Complete details at vim-plug home - https://github.com/junegunn/vim-plug

This will place only `plug.vim` into a new or existing directory struct:


```
curl -fLo ~/.vim/autoload/plug.vim --create-dirs \
    https://raw.githubusercontent.com/junegunn/vim-plug/master/plug.vim
```

### Configuring vim-plug for ensime-vim
Configure plugins and install-directory for `~/.vimrc`:


```
call plug#begin('~/.vim/plugged')

Plug 'ensime/ensime-vim'
Plug 'https://github.com/junegunn/vim-github-dashboard.git'

call plug#end()
```

### Obtain vim-scala

##### Ignore plea to use a saner package manager
##### http://tammersaleh.com/posts/the-modern-vim-config-with-pathogen/

To create the configurations, run this snippet:

```
for d in ftdetect indent syntax; do
  curl -fLo ~/.vim/$d/scala.vim --create-dirs \
      https://raw.githubusercontent.com/derekwyatt/vim-scala/master/$d/scala.vim
done
```



