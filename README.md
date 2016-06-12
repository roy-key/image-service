# Image-service
Image-service will download bunch of images, manipulate and persist them, both in in-memory db and local hd.


##Setup
1. Place image url's in the input.images.txt, seperated with ",".
for example : 
http://carbl.com/im/2013/07/Suzuki-Swift-5d-600x324.jpg,
http://carbl.com/im/2013/06/Cadilalc-CTS-2014-600x324.jpg

2. Configurate wanted width+height px and number of working threads in the config.properties.
notice that number of working threads will be same for all workers types: downloader, manipulators and persister, hence choosing 3 will create a pool with 3 threads for each kind (total of 9).

3. Run main method in the Main class.
4. Enjoy :)
