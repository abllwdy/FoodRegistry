#include "Restaurant.h"
#include <iostream>
#include <iomanip>
#include <cmath>
using namespace std;

Restaurant::Restaurant() {
    // Initialize menu items
    menu.push_back(MenuItem('N', "Nasi Lemak", 9.00));
    menu.push_back(MenuItem('C', "Chicken Rice", 8.00));
    menu.push_back(MenuItem('M', "Masala Dosa", 6.00));
    menu.push_back(MenuItem('H', "Hamburger", 5.00));
    menu.push_back(MenuItem('F', "Fish and Chips", 12.00));

    currentOrder = new Order(menu.size());
    dailySales = new Order(menu.size());
    customerCount = 0;
}

Restaurant::~Restaurant() {
    delete currentOrder;
    delete dailySales;
}

void Restaurant::displayWelcome() {
    cout << "= * === * === * === * === * === * ==" << endl;
    cout << "H & S Restaurant Meal Billing System" << endl;
    cout << "= * === * === * === * === * === * ==" << endl << endl;
}

void Restaurant::displayHelp() {
    cout << "=== HELP " << setw(27) << setfill('=') << "" << endl;
    cout << "List of codes for internal commands:" << endl << endl;
    cout << CMD_CLEAR << " - Clears items in the current bill." << endl;
    cout << CMD_MENU << " - Displays the menu of available items." << endl;
    cout << CMD_HELP << " - Displays this panel." << endl;
    cout << CMD_END << " - Ends the current session and generates a sales report." << endl;
    cout << setw(36) << setfill('=') << "" << endl;
    cout << "How to use:" << endl << endl;
    cout << "Input item code -> quantity" << endl;
    cout << "E.g. 'H' followed by '5' adds 5 Hamburgers" << endl;
    cout << "('-5' removes 5 Hamburgers)" << endl << endl;
    cout << "After confirming the bill," << endl;
    cout << "input the payment received in RM to checkout and close the bill." << endl;
    cout << setw(36) << setfill('=') << "" << endl;
}

void Restaurant::displayMenu() {
    cout << endl << "=== MENU " << setw(27) << setfill('=') << "" << endl << endl;
    for (const auto& item : menu) {
        item.display();
    }
    cout << setw(36) << setfill('=') << "" << endl;
}

bool Restaurant::isValidNumber(const string& str, bool allowDecimal) {
    try {
        float val = stof(str);
        if (allowDecimal) {
            return abs(val) < 524288;
        } else {
            // Check for whole number
            size_t pos = str.find('.');
            if (pos != string::npos) {
                string decimal = str.substr(pos + 1);
                for (char c : decimal) {
                    if (c != '0') return false;
                }
            }
            return abs(val) < 8192;
        }
    } catch (...) {
        return false;
    }
}

int Restaurant::findMenuItemByCode(char code) {
    for (size_t i = 0; i < menu.size(); i++) {
        if (toupper(menu[i].getCode()) == toupper(code)) {
            return i;
        }
    }
    return -1;
}

void Restaurant::processItemCode(char code) {
    int index = findMenuItemByCode(code);
    if (index == -1) {
        cout << "Unrecognised code! Simply input '?' to seek help." << endl;
        return;
    }

    string input;
    cout << "Enter the quantity of " << menu[index].getName() << " to add: ";
    cin >> input;

    if (isValidNumber(input, false)) {
        int quantity = stoi(input);
        if (abs(quantity) < 8192) {
            currentOrder->addItem(index, quantity);
            currentOrder->calculateTotals(menu);
        } else {
            cout << "Sorry, the system does not process massive quantities." << endl;
            cout << "Only input quantities less than 8192." << endl;
        }
    } else {
        cout << "Action cancelled: Item quantity should be integers." << endl;
    }
}

void Restaurant::processPayment(float amount) {
    if (amount < currentOrder->getTotal()) {
        cout << "Insufficient payment received!" << endl;
        cout << "Payment should be at least RM " << fixed 
             << setprecision(2) << currentOrder->getTotal() << endl;
        return;
    }

    customerCount++;
    cout << "Order: #" << customerCount << endl;
    currentOrder->displaySummary(menu, "Items in bill:");
    receiptPrinter.printCheckout(*currentOrder, amount, customerCount);
    receiptPrinter.saveToFile(*currentOrder, menu, amount, customerCount);

    // Add to daily sales
    for (size_t i = 0; i < menu.size(); i++) {
        dailySales->addItem(i, currentOrder->getQuantity(i));
    }
    dailySales->calculateTotals(menu);

    currentOrder->clear();
}

void Restaurant::generateDailyReport() {
    cout << endl << setw(40) << setfill('=') << "" << endl;
    cout << " DAILY MEAL SALES REPORT" << endl;
    cout << setw(40) << setfill('=') << "" << endl;
    cout << "Orders billed: " << customerCount << endl;
    dailySales->displaySummary(menu, "Items sold today:");

    cout << endl << setw(36) << setfill('=') << "" << endl;
    cout << " Subtotal: RM " << fixed << setprecision(2) << dailySales->getSubtotal() << endl;
    cout << " SST charge 10%: RM " << dailySales->getSST() << endl;
    cout << " Grand total: RM " << dailySales->getTotal() << endl;
    cout << setw(36) << setfill('=') << "" << endl;
    cout << "Thank you and have a nice day ahead!" << endl;
}

void Restaurant::run() {
    displayWelcome();
    displayHelp();
    displayMenu();

    bool running = true;
    while (running) {
        cout << endl << setw(36) << setfill('=') << "" << endl << endl;
        cout << "Order: #" << customerCount + 1 << endl;
        currentOrder->displaySummary(menu, "Items in bill:");

        cout << endl << "Enter a code to edit bill/view informations," << endl;
        cout << "or an amount in RM to checkout: ";

        string input;
        cin >> input;
        cout << endl;

        if (isValidNumber(input, true)) {
            float amount = stof(input);
            processPayment(amount);
        } else {
            char cmd = toupper(input[0]);

            switch (cmd) {
                case '<': // Clear
                    currentOrder->clear();
                    break;
                case ',': // Menu
                    displayMenu();
                    break;
                case '?': // Help
                    displayHelp();
                    break;
                case '>': // End session
                    running = false;
                    break;
                default:
                    processItemCode(cmd);
                    break;
            }
        }
    }

    generateDailyReport();
    cout << "Enter anything to exit." << endl;
    string dummy;
    cin >> dummy;
}
