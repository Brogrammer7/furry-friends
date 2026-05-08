This is a pet adoption app I've written entirely in Jetpack Compose using various libraries and technologies, including: Retrofit, Coroutines, and Flows. 

It makes calls to a non-profit Rescue Group's APIs to help find adoptable pets in your local area.

<img width="289" height="289" alt="app_launcher_icon" src="https://github.com/user-attachments/assets/b3d199c2-2b7e-4f22-9d8d-db90930be967" />

App Launcher Icon.

<img width="50%" height="50%" alt="entry_point" src="https://github.com/user-attachments/assets/dc5a63b2-b85c-4493-9385-9e8b0ff3a264" />

Welcome / Login screen.

<img width="50%" height="50%" alt="dashboard_screen" src="https://github.com/user-attachments/assets/888e8f7a-e1a0-40a9-b881-0ebf413fe09c" />

Dashboard screen with intro. Saved pet list feature coming soon!

<img width="50%" height="50%" alt="settings_no_location" src="https://github.com/user-attachments/assets/dd8341b6-bb42-4235-a9b6-44a152fa9321" />

Settings screen (no location set)

<img width="50%" height="50%" alt="settings_location_request_launcher" src="https://github.com/user-attachments/assets/7e299b67-6ecd-4a6e-8282-327b86d65a7f" />

GPS location permission request via Android Permissions system that pulls Lon/Lat coordinates, then converts them into a ZIP Code.

<img width="50%" height="50%" alt="settings_location_detected" src="https://github.com/user-attachments/assets/43a4d2f9-62ff-4a75-a278-cc1e792bbd4b" />

Successful location detection. The DataStore API is employed behind the scenes to store the zip code into memory to survive screen exit and process death. 

<img width="50%" height="50%" alt="search_screen_query" src="https://github.com/user-attachments/assets/de413624-67e7-4951-804a-927c5f3ca564" />

With location stored, we now navigate to the search screen to find our next pet!

<img width="50%" height="50%" alt="search_result_cats" src="https://github.com/user-attachments/assets/14759db3-8381-4b88-badd-14c69534a6d4" />

A successful search result, returning 56 cats in our area. Results are displayed in a LazyColumn.

<img width="50%" height="50%" alt="message_intent1" src="https://github.com/user-attachments/assets/5d62d85b-cdd6-4c17-90e6-4bff669699e8" />

The share button lets us select an app of our choosing to send this pet to a friend.

<img width="50%" height="50%" alt="pet_modal" src="https://github.com/user-attachments/assets/39396283-9dec-42b2-8cdc-3a2822db27fd" />

The phone button brings up a Dialog Composable that gives us contact info and more details on the selected pet. 

<img width="50%" height="50%" alt="dialer_intent" src="https://github.com/user-attachments/assets/c4b2528a-679d-47db-a726-61b523322ab2" />

Clicking the phone number causes a dialer intent picked up by the phone app.

<img width="50%" height="50%" alt="browser_intent" src="https://github.com/user-attachments/assets/6e944c06-e05a-40ff-ac69-3303d2c81aea" />

...and the interactive link takes us to the shelter's webpage on your favorite browser.

<img width="50%" height="50%" alt="search_selection_slider" src="https://github.com/user-attachments/assets/aaa16107-4e12-4481-8d27-7bc32a2fda33" />

The 'Change Animal' button lets us select a different pet type and automatically runs a new query.

<img width="50%" height="50%" alt="search_result_rabbits" src="https://github.com/user-attachments/assets/53107c9e-3d1c-47e9-9c37-b40e238f4b9e" />

New return result with rabbits.

<img width="50%" height="50%" alt="about_screen" src="https://github.com/user-attachments/assets/eb601a57-e3c3-4adb-946f-3efd4edc60ec" />

About screen with information on the developer and commemorative photos of his cat in a scrollable LazyRow along with a playable video.

<img width="50%" height="50%" alt="settings_sign_out" src="https://github.com/user-attachments/assets/86b800cd-0cbd-465c-9f1b-e0d7ba3c0b92" />

We can now sign out of the application and return to the Login page.
