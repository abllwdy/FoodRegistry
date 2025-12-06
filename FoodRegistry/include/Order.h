#ifndef ORDER_H
#define ORDER_H

#include <vector>
#include <string>
#include "MenuItem.h"
using namespace std;

class Order {
private:
    vector<int> quantities;
    int itemCount;
    float subtotal;
    float sst;
    float total;
    const float SST_RATE = 0.10;

public:
    Order(int count);

    void addItem(int index, int quantity);
    int getQuantity(int index) const;
    void calculateTotals(const vector<MenuItem>& menu);

    float getSubtotal() const;
    float getSST() const;
    float getTotal() const;

    bool isEmpty() const;
    void clear();
    void displaySummary(const vector<MenuItem>& menu, const string& message) const;
};

#endif
