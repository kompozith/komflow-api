// src/app/features/contacts/models/contact.ts

export interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  tags: Tag[];
  createdAt: string;
  updatedAt: string;
}

export interface Tag {
  id: string;
  name: string;
  color?: string;
}

export interface ContactPage {
  totalElements: number;
  totalPages: number;
  size: number;
  content: Contact[];
  number: number;
  sort: any;
  numberOfElements: number;
  pageable: any;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ContactFilters {
  page?: number;
  size?: number;
  sort?: string[];
  search?: string;
  tagId?: string;
}

export interface CreateContactRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  tagIds?: string[];
}

export interface UpdateContactRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  tagIds?: string[];
}
