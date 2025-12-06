#include "Order.h"
#include <iostream>
#include <iomanip>
using namespace std;

Order::Order(int count) : itemCount(count), quantities(count, 0), 
                          subtotal(0), sst(0), total(0) {}

void Order::addItem(int index, int quantity) {
    if (index >= 0 && index < itemCount) {
        quantities[index] += quantity;
        if (quantities[index] < 0) quantities[index] = 0;
    }
}

int Order::getQuantity(int index) const {
    return (index >= 0 && index < itemCount) ? quantities[index] : 0;
}

void Order::calculateTotals(const vector<MenuItem>& menu) {
    subtotal = 0;
    for (int i = 0; i < itemCount; i++) {
        subtotal += menu[i].getPrice() * quantities[i];
    }
    sst = subtotal * SST_RATE;
    total = subtotal + sst;
}

float Order::getSubtotal() const { return subtotal; }
float Order::getSST() const { return sst; }
float Order::getTotal() const { return total; }

bool Order::isEmpty() const {
    for (int qty : quantities) {
        if (qty > 0) return false;
    }
    return true;
}

void Order::clear() {
    for (int& qty : quantities) {
        qty = 0;
    }
    subtotal = sst = total = 0;
}

void Order::displaySummary(const vector<MenuItem>& menu, const string& message) const {
    cout << endl << message << endl;
    bool nothing = true;

    for (int i = 0; i < itemCount; i++) {
        if (quantities[i] > 0) {
            nothing = false;
            cout << "(" << menu[i].getCode() << ") [RM " 
                 << fixed << setprecision(2) << menu[i].getPrice() << "] "
                 << menu[i].getName() << " × " << quantities[i] 
                 << " = RM " << menu[i].getPrice() * quantities[i] << endl;
        }
    }

    if (nothing) {
        cout << "Nothing." << endl;
    } else {
        cout << endl << "Amount to be paid: RM " << fixed 
             << setprecision(2) << total << endl;
    }
}
