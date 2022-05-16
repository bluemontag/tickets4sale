import './index.css';
import App from './components/App';
import { QueryClient, QueryClientProvider } from "react-query";
import { createRoot } from 'react-dom/client';

const queryClient = new QueryClient();
const container: any = document.getElementById('root');
const root = createRoot(container);
root.render(
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>);
