# SyncNotes - Markdown Note-Taking App
SyncNotes is a Java-based note-taking application that supports Markdown. It uses Java Swing for the UI, a server-client architecture with Java Sockets, and PostgreSQL for data storage.

## Prerequisites
Before running the project, ensure you have the following installed:

- Java 11+ (Recommended: OpenJDK 17)
- IntelliJ IDEA (Community or Ultimate)
- PostgreSQL (Ensure the database is set up)

## Required Libraries
The project uses external libraries that must be manually added in IntelliJ IDEA. Download and add the following libraries:

- FlatLaf (formdev.flatlaf-3.2)
- FlatLaf IntelliJ Themes (formdev.flatlaf.intellij.themes-3.5)
- RSyntaxTextArea (fifesoft.rsyntaxtextarea-3.6.0)
- Flexmark (vladsch.flexmark.all)
- PostgreSQL JDBC Driver (postgresql-42.7.5)


Setting Up the Project in IntelliJ IDEA
1. Clone or Download the Project
```shell
   git clone https://github.com/your-repository/syncnotes.git
   cd syncnotes
```

2. Open in IntelliJ IDEA

3. Add External Libraries
   - Go to File → Project Structure → Modules → Dependencies.
   - Click Add ( + ) → JARs or Directories.
   - Select the downloaded .jar files for the libraries mentioned above.

4. Set Up Run Configurations
   - The project has two independent main classes:

Client → Client.RegisterForm (Recommended)

Server → Server.Server

Run using intellij idea run button that will automatically include all the required dependency.
