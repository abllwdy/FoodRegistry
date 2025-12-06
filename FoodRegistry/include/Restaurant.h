#ifndef RESTAURANT_H
#define RESTAURANT_H

#include <vector>
#include "MenuItem.h"
#include "Order.h"
#include "Receipt.h"
using namespace std;

class Restaurant {
private:
    vector<MenuItem> menu;
    Order* currentOrder;
    Order* dailySales;
    int customerCount;
    Receipt receiptPrinter;

    const char CMD_CLEAR = '<';
    const char CMD_MENU = ',';
    const char CMD_HELP = '?';
    const char CMD_END = '>';

    void displayWelcome();
    void displayHelp();
    void displayMenu();
    bool isValidNumber(const string& str, bool allowDecimal = true);
    int findMenuItemByCode(char code);
    void processItemCode(char code);
    void processPayment(float amount);
    void generateDailyReport();

public:
    Restaurant();
    ~Restaurant();
    void run();
};

#endif
