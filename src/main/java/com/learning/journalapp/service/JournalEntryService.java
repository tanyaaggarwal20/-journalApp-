package   com.learning.journalApp.service;

import com.learning.journalApp.entity.JournalEntry;
import com.learning.journalApp.entity.User;
import com.learning.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
 public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final UserService userService;

    @Autowired
    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserService userService) {
        this.journalEntryRepository = journalEntryRepository;
        this.userService = userService;
    }

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
         try {
             User user = userService.findByUserName(userName);
             journalEntry.setDate(LocalDateTime.now());
             JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
             user.getJournalEntries().add(savedEntry);
             userService.saveUser(user);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }

    }

    public void saveEntry(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getAll() {
       return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if(removed) {
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
            }

        } catch (Exception e) {
            log.error("Error ", e);
            throw new RuntimeException("An error occurred while deleting the entry.", e);
        }
        return removed;
    }
}

