package org.example.store;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.model.user.User;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class JsonStore {
    private static final Path DATA_DIR  = Paths.get("data");
    private static final Path DATA_FILE = DATA_DIR.resolve("data.json");

    public static class DB {
        public List<User> users = new ArrayList<>();
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private DB db = new DB();

    public JsonStore() throws IOException {
        if (Files.notExists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        if (Files.exists(DATA_FILE)) {
            lock.writeLock().lock();
            try (var r = Files.newBufferedReader(DATA_FILE)) {
                db = mapper.readValue(r, new TypeReference<DB>() {});
            } finally { lock.writeLock().unlock(); }
        } else {
            save();
        }
    }

    private void save() throws IOException {
        Path tmp = DATA_FILE.resolveSibling(DATA_FILE.getFileName() + ".tmp");
        try (var w = Files.newBufferedWriter(tmp)) {
            mapper.writeValue(w, db);
        }
        Files.move(tmp, DATA_FILE,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    public Optional<User> findUserByEmail(String email) {
        lock.readLock().lock();
        try {
            return db.users.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        } finally { lock.readLock().unlock(); }
    }

    public void addUser(User u) throws IOException {
        lock.writeLock().lock();
        try {
            db.users.add(u);
            save();
        } finally { lock.writeLock().unlock(); }
    }

    //para saber dónde quedo grabado el archivo
    public static Path dataFilePath() { return DATA_FILE.toAbsolutePath(); }

    public java.util.List<User> findAllUsers() {
        lock.readLock().lock();
        try { return new java.util.ArrayList<>(db.users); }
        finally { lock.readLock().unlock(); }
    }

    public void updateUser(User updated) throws java.io.IOException {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < db.users.size(); i++) {
                var u = db.users.get(i);
                if (u.getId().equals(updated.getId())) {
                    db.users.set(i, updated);
                    save();
                    return;
                }
            }
            throw new IllegalArgumentException("Usuario no encontrado: " + updated.getEmail());
        } finally { lock.writeLock().unlock(); }
    }

}

