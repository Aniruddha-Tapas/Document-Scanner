<hr>

# Document Scanner


This Document Scanner application turns your mobile into a portable scanner, which can be used for scanning handwritten notes and printed documents.

It automatically detect the edge of the paper over a contrasting surface. When using the printed special page template it automatically detects the QR Code printed on the bottom right corner and scans the page immediately. After the page is detected, it compensates any perspective from the image adjusting it to a 90 degree top view and saves it on a folder on the device.

It also incorporates OCR functionality which the user can use to detect text from documents and save them as editable text files in the external storage of the device.

It is  possible to launch the application from any other application that asks for a picture.

<hr>

## Features

* Scan handwritten or printed documents
* Detects page frame and corrects perspective
* Fast and smooth Image Processing on the fly
* Scans are saved to your device as images
* Detect text using the OCR functionality of the app
* Save the detected texts as editable text files to your device.
* Easily share scanned docs with others via social media, email attachment or sending the doc link.



## Screenshots


<p align="center">
  <img src="https://raw.githubusercontent.com/Aniruddha-Tapas/Capstone-Project/master/Screenshots/Screenshot1.png" height="340" width="190"/>
  <img src="https://raw.githubusercontent.com/Aniruddha-Tapas/Capstone-Project/master/Screenshots/Screenshot2.png" height="340" width="190"/>
  <img src="https://raw.githubusercontent.com/Aniruddha-Tapas/Capstone-Project/master/Screenshots/Screenshot3.png" height="340" width="190"/>
  <img src="https://raw.githubusercontent.com/Aniruddha-Tapas/Capstone-Project/master/Screenshots/Screenshot4.png" height="340" width="190"/>
</p>


## Libraries
1. [OpenCV Android v3.1.0](http://opencv.org/platforms/android.html) for capturing and manipulation of images.
	Due to this version of OpenCV integerated in the app, it needs to run on Android 5.0 (lollipop) or newer. 
	
	### [How to integrate OpenCV into Android Studio](http://stackoverflow.com/questions/27406303/opencv-in-android-studio) 
	
	This stackoverflow link demonstrates the static initialization of OpenCV in Android Studio. The OpenCV native libraries are the main reason for the 38mb sized APK. To avoid this, you can make use of OpenCV Manager application which should to be installed on the android device so as to make OpenCV work.

#### Google Services:

2. [Google Zxing](https://github.com/zxing/zxing) for barcode detection and image processing.

3. [Google Mobile Vision Text API](https://developers.google.com/vision/) to see and understand text using OCR.

4. [Google Analytics](https://developers.google.com/analytics/devguides/collection/android/v4/) to measure user interaction with the app.

	If you want to use Google Analytics in your app, you'll require a configuration file.  You must have a Google Analytics account and a registered property to get the configuration file.
	
	###[Get A Configuration File](https://developers.google.com/mobile/add?platform=android&cntapi=analytics&cnturl=https:%2F%2Fdevelopers.google.com%2Fanalytics%2Fdevguides%2Fcollection%2Fandroid%2Fv4%2Fapp%3Fconfigured%3Dtrue&cntlbl=Continue%20Adding%20Analytics)
	
	Then copy the google-services.json file you just downloaded into the app/ or mobile/ directory of your Android Studio project. Open the Android Studio Terminal pane:
	For Windows : $ move path-to-download/google-services.json app/
	For Mac/Linux : <code>$ mv path-to-download/google-services.json app/</code>
	
5. [Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader) for loading, caching and displaying images in the gallery grid layout.

6. [FABToolbar](https://github.com/fafaldo/FABToolbar) for implementing a Floating Action Button transforming into toolbar.

7. [Drag-Select-RecyclerView](https://github.com/afollestad/drag-select-recyclerview) for Google Photos style multiselection for RecyclerViews.


## External code and References 

This project wouldn't have been possible without the following great resources:

* [Google Mobile Vision Text API](https://developers.google.com/vision/text-overview)
* [Android-er / GridView code sample](http://android-er.blogspot.com.br/2012/07/gridview-loading-photos-from-sd-card.html)
* [Android Hive / Full Screen Image pager](http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/)
* [Adrian Rosebrock from pyimagesearch.com for the excellent tutorial on how to handle the images](http://www.pyimagesearch.com/2014/09/01/build-kick-ass-mobile-document-scanner-just-5-minutes/)
* [Gabriele Mariotti / On how to implement sections in the RecyclerView](https://gist.github.com/gabrielemariotti/e81e126227f8a4bb339c)

not to forget:
* A lot of coffee and the ever dependable [stackoverflow.com](http://stackoverflow.com/)

<hr>
