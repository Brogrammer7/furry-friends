This is a pet adoption app I've written entirely in Jetpack Compose using various libraries and technologies, including: Retrofit, Coroutines, and Flows. 

It makes calls to a non-profit Rescue Group's APIs to help find adoptable pets in your local area.

App Launcher Icon

![FF1](https://github.com/user-attachments/assets/b7aa6a81-a924-41c7-90b9-77ceb86eb3d3)

App automatically runs Retrofit API query after ZIP Code is entered from keyboard input or if info was saved in Settings

![FF2](https://github.com/user-attachments/assets/9fc19071-7df8-4d1f-8bf0-ac607aa06e7f)

Search results from real available pets displayed in a LazyColumn

![FF3](https://github.com/user-attachments/assets/21478ed1-e2f2-4884-aea7-1d73b9eaddef)

Details button clicked launches modal with pet's contact info with phone number and link that dial and navigate to web browser via Android Intents system

![FF4](https://github.com/user-attachments/assets/4e34d168-163f-43fc-a496-81c7acc5ae4c)

Settings screen that prompts user via Android Permissions system and uses GPS to pull Lon/Lat coordinates, then converts them into a ZIP Code:

![FF5](https://github.com/user-attachments/assets/b3540f4e-7bc6-40c8-b68b-6d0f8bfd0120)

User's retrieved ZIP Code is now stored and will automatically populate and run a search query with this info once they navigate to Search Screen

![FF6](https://github.com/user-attachments/assets/205ea22b-696d-4f99-a6ac-bdb69a11ac01)


