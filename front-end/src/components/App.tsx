import './App.css';
import "./table/Table.css";
import { useQuery } from "react-query";
import apiClient from "../commons/http-common";
import GenreDetail from './genre-detail/GenreDetail';
import { FormEvent, useState } from 'react';
import InventoryItem from '../models/InventoryItem';


function App() {

  const currentDateStr = new Date().toISOString().split('T')[0];

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    const t = event.target as typeof event.target & { showDate: { value: string }, queryDate: { value: string } };

    const queryDate = t.queryDate.value;
    const showDate = t.showDate.value;

    if (isValidDate(queryDate) && isValidDate(showDate)) {
      console.log('Sending query');
      findShowInfo();
    } else {
      alert('Please enter a valid date in format YYYY-MM-DD');
    }
  }

  const [queryDate, setQueryDate] = useState(currentDateStr);
  const defShowDate = '2022-10-01';
  const [showDate, setShowDate] = useState(defShowDate);

  function isValidDate(dateString: string) {
    var regEx = /^\d{4}-\d{2}-\d{2}$/;
    if (!dateString.match(regEx)) return false;  // Invalid format
    var d = new Date(dateString);
    var dNum = d.getTime();
    if (!dNum && dNum !== 0) return false; // NaN value, Invalid date
    return d.toISOString().slice(0, 10) === dateString;
  }

  const defInventoryItems: InventoryItem[] = [];
  const [inventoryData, setInventoryData] = useState(defInventoryItems);

  const [loading, setLoading] = useState(false);

  const { refetch: findShowInfo } =
    useQuery(
      "getShows",
      async () => {
        setLoading(true);
        return await apiClient.get(`/shows/${queryDate}/${showDate}`);
      },
      {
        enabled: false,
        retry: 1,
        onSuccess: (res) => {
          console.log('Receiving data:');
          console.log(res.data.inventory);
          setInventoryData(res.data.inventory as InventoryItem[]);
          setLoading(false);
        },
        onError: (err) => {
          alert('Error on fetching data from the server ' + err)
          setInventoryData([]);
          setLoading(false);
        },
      }
    );

  return (
    <div className="App">
      <header className="App-header">
        <h1>Tickets4Sale</h1>

        <form onSubmit={handleSubmit}>

          <label htmlFor='queryDate'>Query Date:</label>
          <input type='text' name='queryDate' placeholder='YYYY-MM-DD' value={queryDate} onChange={e => setQueryDate(e.target.value)} />

          <label htmlFor='showDate'>Show Date:</label>
          <input type='text' name='showDate' placeholder='YYYY-MM-DD' value={showDate} onChange={e => setShowDate(e.target.value)} />

          <button type='submit'>Send form</button>
          <p>&nbsp;</p>
        </form>

        { loading &&
          'Loading info...'
        }

        {inventoryData.length > 0 &&
          inventoryData.map((item, order) => {
            return <GenreDetail key={order} genre={item.genre} showsData={item.shows} />
          })}

        { !loading && inventoryData.length === 0 &&
          'No Data Found.'
        }

      </header>
    </div>
  );
}

export default App;
