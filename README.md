# ZenWord - Wordle-Style Game

ZenWord is a word game inspired by Wordle, developed for Android devices using Android Studio. The goal of the game is to guess hidden words from given letters while accumulating points and discovering additional words.

## Features

- **Intuitive User Interface**: Designed for smooth user interaction.
- **Hidden Words**: Players must guess hidden words using available letters.
- **Scoring System**: Players earn points for each correctly guessed word.
- **Bonuses**: Players can earn bonuses to help reveal letters in hidden words.
- **Customizable Colors**: The interface includes color schemes that can be randomly changed.

## System Requirements

- Android Studio
- Android SDK
- Android device or emulator

## Installation

1. Clone this repository to your local machine:
   
   ```bash
   git clone https://github.com/joacoesperon/ZenWord
   ```

2. Open the project in Android Studio.

3. Ensure that the Android SDK is properly configured.

4. Run the application on a device or emulator.

## How to Play

1. When the game starts, a random hidden word is generated.
2. Players select letters from a set of buttons.
3. Selected letters are added to the current word attempt.
4. Players must guess the hidden word before running out of attempts.

## Project Structure

```
ZenWord/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/zenword/
│   │   │   │   ├── MainActivity.java  # Main game logic
│   │   │   │   ├── WordManager.java  # Handles word selection
│   │   │   │   ├── ScoreManager.java  # Manages scoring system
│   │   │   ├── res/
│   │   │   │   ├── layout/  # XML layout files
│   │   │   │   ├── drawable/  # Icons and images
│   │   │   │   ├── values/  # Colors, strings, and themes
```

## Contributions

Contributions are welcome! If you would like to contribute to this project, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/new-feature`).
3. Make your changes and commit (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature/new-feature`).
5. Open a Pull Request.

## Authors

- **Joaquín Esperón**

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Contact

For more information, you can contact [joacoesperon1@gmail.com](mailto:joacoesperon1@gmail.com).

