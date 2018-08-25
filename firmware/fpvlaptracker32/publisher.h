#pragma once

#include <Arduino.h>
#include "subscriber.h"

namespace pubsub {

    template <class T> class Publisher {
    public:
        Publisher(): _subscriber1(NULL), _subscriber2(NULL), _subscriber3(NULL) {}

        virtual ~Publisher(){}

        void addSubscriber(pubsub::Subscriber<T> *subscriber){
            if (_subscriber1 == NULL) {
                _subscriber1 = subscriber;
            } else if (_subscriber2 == NULL) {
                _subscriber2 = subscriber;
            } else if (_subscriber3 == NULL) {
                _subscriber3 = subscriber;
            }
        }

        void notifySubscribers(T value) {
            if (_subscriber1 != NULL) {
                _subscriber1->update(value);
            }
            if (_subscriber2 != NULL) {
                _subscriber2->update(value);
            }
            if (_subscriber3 != NULL) {
                _subscriber3->update(value);
            }
        }
    private:
        Subscriber<T> *_subscriber1;
        Subscriber<T> *_subscriber2;
        Subscriber<T> *_subscriber3;
    };

}