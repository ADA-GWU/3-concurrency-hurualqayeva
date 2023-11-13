# Image Processing Application

This Java-based application allows users to process images by averaging colors within defined square regions. The program supports both single-threaded and multi-threaded processing methods for enhanced efficiency.

## Features

- **Open Image**: Select an image for processing.
- **Adjustable Square Size**: Modify the square area used for color averaging.
- **Processing Options**: Choose between single-threaded or multi-threaded processing.
- **Save Processed Image**: Save the resulting image after processing.

## Requirements

- **Java Runtime Environment (JRE)**: Ensure you have Java installed to run the application.

## Usage

1. **Running the Application**:
   - Execute the `main` method in the `App` class.
   - ```
     cd src
     javac App.java
     java App.java
     ```
   
2. **GUI Functions**:
   - **Open Image**: Select an image file to process.
   - **Square Size**: Adjust the square size for color averaging.
   - **Processing Options**: Select single or multi-threaded processing.
   - **Process Image**: Initiate the image processing.
   - **Save Image**: Save the processed image.

## Libraries Used

- **Swing**: Used for the graphical interface.
- **ImageIO**: Employed for image input/output handling.
- **AWT**: Utilized for basic image manipulation.

## Running the Application

1. **Open the Project in an IDE**.
2. **Run the Application** by executing the `main` method in the `App` class.
3. **Use the GUI Controls** to load, process, and save images.
