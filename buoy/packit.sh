[ -e $IMAGE_NAME ] && { echo Please set IMAGE_NAME in environment; exit 1; }
pack build $IMAGE_NAME/buoy-pack --builder=cloudfoundry/cnb:bionic --path=.
