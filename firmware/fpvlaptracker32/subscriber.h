#pragma once

#include <Arduino.h>

namespace pubsub {

    template <class T>class Subscriber {
    public:
        Subscriber() {}
        virtual ~Subscriber() {}
        virtual void update(T value) = 0;
    };

}