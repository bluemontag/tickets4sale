import React from 'react';
import Table from '../table/Table';
import { useMemo, useState, useEffect} from "react";
import Show from '../../models/Show';

interface IProps {
    genre: string;
    showsData: Show[];
}

const GenreDetail: React.FC<IProps> = ({ genre, showsData }) => {

    const [data, setData] = useState(showsData);

    // Using useEffect to call the API once mounted and set the data
    useEffect(() => {
        setData(showsData);
    }, [showsData]);

    const columns = useMemo(
        () => [
            {
                Header: 'GENRE: ' + genre,
                columns: [
                    {
                        Header: "Title",
                        accessor: "title"
                    },
                    {
                        Header: "Opening Date",
                        accessor: "opening"
                    },
                    {
                        Header: "Tickets Left",
                        accessor: "ticketsLeft"
                    },
                    {
                        Header: "Tickets Available",
                        accessor: "ticketsAvailable"
                    },
                    {
                        Header: "Status",
                        accessor: "status"
                    },
                    {
                        Header: "Price",
                        accessor: "price"
                    }
                ]
            }
        ],
        [genre]
    );

    return (
        <>
            <Table columns={columns} data={data} />

            <p>&nbsp;</p>
        </>

    );
}


export default GenreDetail;