import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import pandas_datareader as web
import datetime as dt
from datetime import timedelta, date

from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, LSTM
from yahoo_fin.stock_info import get_data


start = dt.datetime(2012, 1, 1)
end = dt.datetime.now()

# Load Data
ticker = "GOOG"
data = get_data(ticker, start_date=start, end_date=end, index_as_date=True, interval='1d')
# end = dt.datetime(2020, 1, 1)

df = pd.DataFrame(data)

dates = []
dates = df.axes[0].tolist()
dates = [date_obj.strftime('%Y-%m-%d') for date_obj in dates] # x coords

stock_prices = df['close'].tolist() 
stock_prices = [str(stock) for stock in stock_prices] # y coords

# Prepare Data
scaler = MinMaxScaler(feature_range=(0,1)) # Scale down all values to fit between 0 and 1

scaled_data = scaler.fit_transform(data['close'].values.reshape(-1, 1)) # Closing Price

# How many days to look back to forecast future price
prediction_days = 60

x_train = []
y_train = []

for x in range(prediction_days, len(scaled_data)):
	x_train.append(scaled_data[x - prediction_days:x])
	y_train.append(scaled_data[x, 0])

x_train, y_train = np.array(x_train), np.array(y_train) # Convert to numpy arrays
 
x_train = np.reshape(x_train, (x_train.shape[0], x_train.shape[1], 1))


# Data Model
model = Sequential()

# LSTM -> Dropout, Dense at the end
# Adjust number of layers, (More -> Accurate, Slower) (Less -> Less Accurate, Faster)
model.add(LSTM(units=50, return_sequences=True, input_shape=(x_train.shape[1], 1)))
model.add(Dropout(0.2))

model.add(LSTM(units=50, return_sequences=True))
model.add(Dropout(0.2))

model.add(LSTM(units=50))
model.add(Dropout(0.2))

# Final Layer
model.add(Dense(units=1)) # Prediction of the next closing price

model.compile(optimizer='adam', loss='mean_squared_error')
model.fit(x_train, y_train, epochs=25, batch_size=32) # 32 units at once (batch size)

# Testing Model Accuracy on Existing Data

# Load Test Data
test_start = dt.datetime(2020, 1, 1)
test_end = dt.datetime.now()


test_data = get_data("AMZN", start_date=test_start, end_date=test_end, index_as_date=True, interval='1d')

actual_prices = test_data['close'].values # Values from the stock market, not predicted ones

total_dataset = pd.concat((data['close'], test_data['close']), axis=0)

model_inputs = total_dataset[len(total_dataset) - len(test_data) - prediction_days:].values
model_inputs = model_inputs.reshape(-1, 1)
model_inputs = scaler.transform(model_inputs)

# test_stock_prices = [str(stock) for stock in stock_prices] # y coords for test graph

# Predictions on Test Data
x_test = []

for x in range(prediction_days, len(model_inputs)):
	x_test.append(model_inputs[x-prediction_days:x, 0])

x_test = np.array(x_test)
x_test = np.reshape(x_test, (x_test.shape[0], x_test.shape[1], 1))
# print(x_test)
predicted_prices = model.predict(x_test)
predicted_prices = scaler.inverse_transform(predicted_prices)

real_data = [model_inputs[len(model_inputs) + 1 - prediction_days:len(model_inputs) + 1, 0]]

real_data = np.array(real_data)
real_data = np.reshape(real_data, (real_data.shape[0], real_data.shape[1], 1))


prediction = model.predict(real_data)
prediction = scaler.inverse_transform(prediction)

prediction_num = np.array_str(prediction)

print(prediction_num)


# print(f"Prediction: {prediction}")
