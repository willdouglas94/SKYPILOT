import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../environments/environment';

interface Airline {
  id: string;
  name: string;
  iata: string;
  icao: string;
  country: string;
  baseCity: string;
  mainAirportCode: string;
}

interface Airport {
  code: string;
  city: string;
  country: string;
}

interface Aircraft {
  id: string;
  registration: string;
  status: string;
  aircraftType?: {
    model: string;
    category: string;
  };
}

interface Route {
  id: string;
  origin: Airport;
  destination: Airport;
  distanceKm: number;
  durationMinutes: number;
}

interface FlightOffer {
  id: string;
  date: string;
  departureTime: string;
  status: string;
  route: Route;
  aircraft: Aircraft;
}

interface DashboardSummary {
  airlines: number;
  aircraft: number;
  routes: number;
  offers: number;
  pendingOffers: number;
  acceptedOffers: number;
  rejectedOffers: number;
  dispatchReadyOffers: number;
}

interface DispatchBoardItem {
  offerId: string;
  routeCode: string;
  aircraftRegistration: string;
  status: string;
  dispatchReady: boolean;
  flightWindow: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly apiBaseUrl = environment.apiBaseUrl;

  airlines: Airline[] = [];
  routes: Route[] = [];
  aircraftList: Aircraft[] = [];
  offers: FlightOffer[] = [];
  dispatchBoard: DispatchBoardItem[] = [];
  selectedRouteId = '';
  selectedAircraftId = '';
  selectedStatusFilter = 'ALL';
  selectedRouteFilter = 'ALL';
  departureTime = '09:40';
  offerDate = '2026-09-01';
  username = '';
  password = '';
  token = '';
  isAuthenticated = false;
  dashboard: DashboardSummary = {
    airlines: 0,
    aircraft: 0,
    routes: 0,
    offers: 0,
    pendingOffers: 0,
    acceptedOffers: 0,
    rejectedOffers: 0,
    dispatchReadyOffers: 0,
  };
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.restoreSession();
    this.loadCatalog();
  }

  private restoreSession(): void {
    const savedToken = localStorage.getItem('skypilot-token');
    if (savedToken) {
      this.token = savedToken;
      this.isAuthenticated = true;
    }
  }

  private saveSession(): void {
    localStorage.setItem('skypilot-token', this.token);
  }

  private clearSession(): void {
    this.token = '';
    this.isAuthenticated = false;
    localStorage.removeItem('skypilot-token');
  }

  private apiUrl(path: string): string {
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${this.apiBaseUrl}${normalizedPath}`;
  }

  private async apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers || {});

    if (this.token) {
      headers.set('Authorization', `Bearer ${this.token}`);
    }

    if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const bodyText = await response.text();
      throw new Error(bodyText || 'Erro da API');
    }

    return response.json() as Promise<T>;
  }

  async registerUser(): Promise<void> {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Informe usuário e senha.';
      return;
    }

    try {
      await this.apiFetch(this.apiUrl('/api/auth/register'), {
        method: 'POST',
        body: JSON.stringify({ username: this.username.trim(), password: this.password }),
      });

      this.successMessage = 'Usuário criado com sucesso. Faça login para continuar.';
      this.errorMessage = '';
      this.password = '';
    } catch (error) {
      this.successMessage = '';
      this.errorMessage = error instanceof Error ? error.message : 'Não foi possível cadastrar o usuário.';
    }
  }

  async loginUser(): Promise<void> {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Informe usuário e senha.';
      return;
    }

    try {
      const payload = await this.apiFetch<{ token: string }>(this.apiUrl('/api/auth/login'), {
        method: 'POST',
        body: JSON.stringify({ username: this.username.trim(), password: this.password }),
      });

      this.token = payload.token;
      this.isAuthenticated = true;
      this.saveSession();
      this.successMessage = 'Login realizado com sucesso.';
      this.errorMessage = '';
      this.password = '';
      await this.loadCatalog();
    } catch (error) {
      this.clearSession();
      this.successMessage = '';
      this.errorMessage = error instanceof Error ? error.message : 'Não foi possível autenticar.';
    }
  }

  logout(): void {
    this.clearSession();
    this.successMessage = 'Sessão encerrada.';
    this.errorMessage = '';
    this.airlines = [];
    this.routes = [];
    this.aircraftList = [];
    this.offers = [];
  }

  async loadCatalog(): Promise<void> {
    if (!this.isAuthenticated || !this.token) {
      return;
    }

    try {
      const [airlinesResponse, routesResponse, aircraftResponse, offersResponse, dashboardResponse, dispatchBoardResponse] = await Promise.all([
        this.apiFetch<Airline[]>(this.apiUrl('/api/airlines')),
        this.apiFetch<Route[]>(this.apiUrl('/api/routes')),
        this.apiFetch<Aircraft[]>(this.apiUrl('/api/aircraft')),
        this.apiFetch<FlightOffer[]>(this.apiUrl('/api/offers')),
        this.apiFetch<DashboardSummary>(this.apiUrl('/api/dashboard')),
        this.apiFetch<DispatchBoardItem[]>(this.apiUrl('/api/dispatch-board')),
      ]);

      this.airlines = airlinesResponse;
      this.routes = routesResponse;
      this.aircraftList = aircraftResponse;
      this.offers = offersResponse;
      this.dashboard = dashboardResponse;
      this.dispatchBoard = dispatchBoardResponse;

      if (!this.selectedRouteId && this.routes.length > 0) {
        this.selectedRouteId = this.routes[0].id;
      }

      if (!this.selectedAircraftId && this.aircraftList.length > 0) {
        this.selectedAircraftId = this.aircraftList[0].id;
      }

      this.errorMessage = '';
    } catch (error) {
      this.clearSession();
      this.errorMessage = 'Sessão expirada ou backend indisponível. Faça login novamente.';
      console.error(error);
    }
  }

  async createOffer(): Promise<void> {
    if (!this.selectedRouteId || !this.selectedAircraftId) {
      this.errorMessage = 'Selecione uma rota e uma aeronave.';
      return;
    }

    try {
      await this.apiFetch(this.apiUrl('/api/offers'), {
        method: 'POST',
        body: JSON.stringify({
          routeId: this.selectedRouteId,
          aircraftId: this.selectedAircraftId,
          departureTime: this.departureTime,
          date: this.offerDate,
        }),
      });

      this.successMessage = 'Oferta criada com sucesso.';
      this.errorMessage = '';
      await this.loadCatalog();
    } catch (error) {
      this.successMessage = '';
      this.errorMessage = error instanceof Error ? error.message : 'Não foi possível criar a oferta.';
      console.error(error);
    }
  }

  async updateOfferStatus(offerId: string, status: 'ACCEPTED' | 'REJECTED'): Promise<void> {
    try {
      await this.apiFetch(this.apiUrl(`/api/offers/${offerId}/status`), {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      });

      this.successMessage = `Oferta ${status === 'ACCEPTED' ? 'aceita' : 'rejeitada'} com sucesso.`;
      this.errorMessage = '';
      await this.loadCatalog();
    } catch (error) {
      this.successMessage = '';
      this.errorMessage = error instanceof Error ? error.message : 'Não foi possível atualizar a oferta.';
      console.error(error);
    }
  }

  get filteredOffers(): FlightOffer[] {
    return this.offers.filter((offer) => {
      const matchesStatus = this.selectedStatusFilter === 'ALL' || offer.status === this.selectedStatusFilter;
      const matchesRoute = this.selectedRouteFilter === 'ALL' || offer.route.id === this.selectedRouteFilter;
      return matchesStatus && matchesRoute;
    });
  }

  formatDate(value: string): string {
    if (!value) {
      return '—';
    }

    return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }
}
