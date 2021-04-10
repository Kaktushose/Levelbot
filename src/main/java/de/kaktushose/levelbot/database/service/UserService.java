package de.kaktushose.levelbot.database.service;

import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.database.repositories.ItemRepository;
import de.kaktushose.levelbot.database.repositories.TransactionRepository;
import de.kaktushose.levelbot.database.repositories.UserRepository;
import de.kaktushose.levelbot.spring.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;

    public UserService() {
        ApplicationContext context = ApplicationContextHolder.getContext();
        userRepository = context.getBean(UserRepository.class);
        transactionRepository = context.getBean(TransactionRepository.class);
        itemRepository = context.getBean(ItemRepository.class);
    }

    public List<BotUser> getAll() {
        List<BotUser> result = new ArrayList<>();
        userRepository.findAll().forEach(result::add);
        return result;
    }

    public BotUser getById(long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    public List<BotUser> getByPermission(int permissionLevel) {
        return userRepository.findByPermissionLevel(permissionLevel);
    }

    public BotUser create(long userId) {
        return userRepository.save(new BotUser(userId));
    }

    public BotUser createIfAbsent(long userId) {
        Optional<BotUser> optional = userRepository.findById(userId);
        if (optional.isEmpty()) {
            return create(userId);
        }
        return optional.get();
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public void exchangeDiamonds(long userId, long diamonds) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(botUser.getDiamonds() - diamonds);
        botUser.setCoins(botUser.getCoins() + diamonds * 40);
        userRepository.save(botUser);
    }

    public boolean hasItem(long userId, int itemId) {
        return transactionRepository.findByUserIdAndItemId(userId, itemId).isPresent();
    }

    public void buyItem(long userId, int itemId) {
        BotUser botUser = getById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        Transaction transaction = new Transaction();
        transaction.setBuyTime(System.currentTimeMillis());
        transaction.setItem(item);
        botUser.getTransactions().add(transaction);
        botUser.setCoins(botUser.getCoins() - item.getPrice());
        transactionRepository.save(transaction);
        userRepository.save(botUser);
    }

    public void addUpItem(long userId, int itemId) {
        BotUser botUser = getById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow();
        Optional<Transaction> optional = transactionRepository.findByUserIdAndItemId(userId, itemId);
        Transaction transaction;
        if (optional.isPresent()) {
            transaction = optional.get();
            transaction.setBuyTime(transaction.getBuyTime() + item.getDuration());
        } else {
            transaction = new Transaction();
            transaction.setBuyTime(System.currentTimeMillis());
            transaction.setItem(item);
        }
        botUser.getTransactions().add(transaction);
        userRepository.save(botUser);
        transactionRepository.save(transaction);
    }

    public List<Item> getItems(long userId) {
        BotUser botUser = getById(userId);
        return botUser.getTransactions().stream().map(Transaction::getItem).collect(Collectors.toList());
    }

    public boolean ownsItemOfCategory(long userId, int categoryId) {
        List<Item> userItems = getItems(userId);
        return itemRepository.findByCategoryId(categoryId).stream().anyMatch(userItems::contains);
    }

    public void removeItem(long userId, int itemId) {
        BotUser botUser = getById(userId);
        botUser.getTransactions().removeIf(transaction -> transaction.getItem().getItemId() == itemId);
        userRepository.save(botUser);
    }

    public boolean switchDaily(long userId) {
        BotUser botUser = getById(userId);
        botUser.setDailyUpdate(!botUser.isDailyUpdate());
        return userRepository.save(botUser).isDailyUpdate();
    }

    public void setPermission(long userId, int permissionLevel) {
        BotUser botUser = getById(userId);
        botUser.setPermissionLevel(permissionLevel);
        userRepository.save(botUser);
    }

    public long addCoins(long userId, long amount) {
        BotUser botUser = getById(userId);
        botUser.setCoins(botUser.getCoins() + amount);
        userRepository.save(botUser);
        return botUser.getCoins();
    }

    public long addXp(long userId, long amount) {
        BotUser botUser = getById(userId);
        botUser.setXp(botUser.getXp() + amount);
        userRepository.save(botUser);
        return botUser.getXp();
    }

    public long addDiamonds(long userId, long amount) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(botUser.getDiamonds() + amount);
        userRepository.save(botUser);
        return botUser.getDiamonds();
    }

    public void setCoins(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setCoins(amount);
        userRepository.save(botUser);
    }

    public void setXp(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setXp(amount);
        userRepository.save(botUser);
    }

    public void setDiamonds(long userId, int amount) {
        BotUser botUser = getById(userId);
        botUser.setDiamonds(amount);
        userRepository.save(botUser);
    }

    public void updateLastValidMessage(long userId) {
        BotUser botUser = getById(userId);
        botUser.setLastValidMessage(System.currentTimeMillis());
        userRepository.save(botUser);
    }

    public void updateMessageCount(long userId) {
        BotUser botUser = getById(userId);
        botUser.setMessageCount(botUser.getMessageCount() + 1);
        userRepository.save(botUser);
    }

    public int increaseRank(long userId) {
        BotUser botUser = getById(userId);
        if (botUser.getLevel() == 10) {
            return 10;
        }
        botUser.setLevel(botUser.getLevel() + 1);
        userRepository.save(botUser);
        return botUser.getLevel();
    }

}
