[ -e $IMAGE_NAME ] && { echo Please set IMAGE_NAME in environment; exit 1; }
./mvnw package -DskipTests
